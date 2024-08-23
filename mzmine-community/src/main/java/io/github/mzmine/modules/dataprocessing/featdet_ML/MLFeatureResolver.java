/*
 * Copyright (c) 2004-2024 The mzmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.modules.dataprocessing.featdet_ML;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.AbstractResolver;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.scans.PeakPickingModel.PeakPickingModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MLFeatureResolver extends AbstractResolver {
    
    private final ParameterSet parameters;
    private final double threshold;
    private final int inputLength;
    private final PeakPickingModel model;
    double[] xBuffer;
    double[] yBuffer;

    public MLFeatureResolver(ParameterSet parameterSet, ModularFeatureList flist){
        super(parameterSet, flist);
        this.parameters = parameterSet;
        //THRESHOLD FIXED FOR DEBUGGING ONLY!
        this.threshold = 0.5;
        this.inputLength = 128;
        this.model = new PeakPickingModel(); 
    }
    
    @Override 
    public Class<? extends MZmineProcessingModule> getModuleClass(){
        return MLFeatureResolverModule.class;
    }
    
    public List<Range<Double>> resolve(double[] x, double[] y){
        if (x.length != y.length){
            throw new AssertionError("Lengths of x, y and indices array do not match.");
        }
        
        final int inputLength = x.length;

        List<double[]> standardRegions = SplitSeries.extractRegionBatch(x);
        //CURRENTLY the is padding to the righ by zeros.
        List<double[]> standardRegionsRT = SplitSeries.extractRegionBatch(y);
        List<Map<String,double[]>> resolvedRegions;
        try{
        resolvedRegions =  model.predictor.batchPredict(standardRegions); 
        } catch(Exception e){
            System.out.println("Error during prediction.");
            e.printStackTrace();
            return null;
        } 
        //Converts the prediction to booleans based on >threshold or <threshold
        //Change this stream to something cleaner
        List<boolean[]> predLabels = resolvedRegions.stream()
            .map(r-> 
                Arrays.stream(r.get("probs"))
                .mapToObj(prob -> (prob > threshold))
                .toArray(Boolean[]::new))
                .map(arr -> {
                    boolean[] boolArr = new boolean[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        boolArr[i] = arr[i];
                    }
                    return boolArr;
                })
                //.toArray(boolean[]::new))
            .collect(Collectors.toList());
        //Rounds the predictions to Indices and make sure they are insde the bounds
        List<int[]> leftIndices = resolvedRegions.stream()
            .map(r-> 
                Arrays.stream(r.get("left"))
                .mapToInt(d -> (int) Math.round(d))
                .map(i -> Math.max(i,0))
                .toArray())
            .collect(Collectors.toList());
        List<int[]> rightIndices = resolvedRegions.stream()
            .map(r-> 
                Arrays.stream(r.get("right"))
                .mapToInt(d -> (int) Math.round(d))
                .map(i -> Math.min(i, inputLength-1))
                .toArray())
            .collect(Collectors.toList());
        
        //Converts indices to retention times and writes times into regions 
        //Getting the compiler to infer the correct types here is horrible
        int lenList = predLabels.size();
        int lenFeatures = predLabels.get(0).length;
        List<Range<Double>[]> allRanges = IntStream.range(0,lenList)
            .mapToObj(i ->
                IntStream.range(0,lenFeatures)
                .mapToObj(j -> (Range<Double>) Range.closed(
                    Double.valueOf(standardRegionsRT.get(i)[leftIndices.get(i)[j]]),
                    Double.valueOf(standardRegionsRT.get(i)[rightIndices.get(i)[j]])
                    )) 
                    .toArray(size -> (Range<Double>[]) new Range[size])
                )
                .collect(Collectors.toList());
            
            //Filter out only those regions where a peak is predictend
            // !Maybe do the filtering before do you don't create unnecessary instances of Range<Double>!
            List<Range<Double>> resolved = new ArrayList<>();
            IntStream.range(0,lenList)
                .forEach(i ->
                IntStream.range(0,lenFeatures)
                .forEach(j -> {
                    if(predLabels.get(i)[j]){
                        resolved.add(allRanges.get(i)[j]);
                    }
                })
            );
            
            //Remove duplicate peaks from overlapping regions
            List<Range<Double>> uniqueResolved = new ArrayList<>();
            int lenUnique = uniqueResolved.size();
            for(int i=0; i<lenUnique; i++){
                //checks overlap for all ranges from index start to i
                int start = Math.max(0, i-10);
                boolean overlap = false;
                for(int j=start;j<i;j++){
                    Range<Double> intersection = resolved.get(i).intersection(resolved.get(j));
                    if((intersection.upperEndpoint()-intersection.lowerEndpoint())>3){
                        overlap = true;
                    }
                }
                if(!overlap){
                    uniqueResolved.add(resolved.get(i));
                }
            }
 
        return uniqueResolved;
    }

    public void closeModel() {
        if(model != null){
            model.closeModel();
        }
    }
    
    public static void main(String[] args){
        System.out.println("test test 123");
    }
}
