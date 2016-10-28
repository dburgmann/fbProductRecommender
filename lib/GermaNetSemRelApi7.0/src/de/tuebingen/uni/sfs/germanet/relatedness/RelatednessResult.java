/*
 * Copyright (C) 2012 Department of General and Computational Linguistics,
 * University of Tuebingen
 *
 * This file is part of the Java Relatedness API to GermaNet.
 *
 * The Java Relatedness API to GermaNet is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Java Relatedness API to GermaNet is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this API; if not, see <http://www.gnu.org/licenses/>.
 */
package de.tuebingen.uni.sfs.germanet.relatedness;

import java.util.*;

/**
 * Represents the result of a relatedness mesaure (class Relatedness).
 * Contains the name of the measure and the result and can calculate the
 * normalized result (mapping into [0,1]).
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class RelatednessResult {
    
    private final String measure; //name of the measure used
    private final double result;  //result of this measure
    private final Object[] args;  //contains additional arguments (for lesk and ics)

    /**
     * Constructor taking name of the relatedness measure used and result value.
     * @param name the name of the relatedness measure used
     * @param value the result of this relatedness measure
     * @param arguments additional args for lesk and ics; null for path measures
     */
    public RelatednessResult (String name, double value, Object[] arguments) {
        measure = name;
        result = value;
        if (null != arguments) {
            args = arguments;
        } else {
            args = new Object[0];
        }
    }

    /**
     * Returns the double value calculated by this relatedness measure.
     * @return the result value of the relatedness calculation
     */
    public double getResult() {
        return result;
    }

    /**
     * Returns a mapping of the result value into [0,1], where 0 is no similarity
     * and 1 is identity. <br>
     * Caution: This is not true for Resnik's measure. Identity
     * may have a value smaller than 1 for Resnik. 
     * @return a normalized representation of the given relatedness value
     */
    public double getNormalizedResult() {
        if (Double.compare(result,-1) == 0) { //invalid result
            return result;
        }
        if (measure.equalsIgnoreCase("path")) {
            return result; //already is  0 <= x <= 1
        } else if (measure.equalsIgnoreCase("wup")) {
            return result; //already is  0 <= x <= 1
        } else if (measure.equalsIgnoreCase("lch")) {
            if (Double.compare(result,0) == 0) {
                return result;
            }
            //max value = leaf node identity = basis for normalization =~ 3.71
            return result*1.0/(-Math.log(1.0/(2.0*Relatedness.MAX_DEPTH+1)));
        } else if (measure.equalsIgnoreCase("res")) {
            if (args.length<1) { //should never happen
                System.out.println("Error: Missing arguments for res similarity.");
                System.exit(0);
            }
            double rootFreq = (Double)args[0];
            if (Double.compare(result,0) == 0) {
                return result;
            }
            //max value = ic(least frequent) = ic(root) = basis for normalization
            //=~  18.75
            return result*1.0/(-Math.log(1.0/rootFreq));
        } else if (measure.equalsIgnoreCase("lin")) {
            return result;
        } else if (measure.equalsIgnoreCase("jcn")) {
            if (args.length<1) { //should never happen
                System.out.println("Error: Missing arguments for jcn similarity.");
                System.exit(0);
            }
            if (Double.compare(result,0) == 0) {
                return result;
            }
            HashMap<String,Integer> freqs = (HashMap<String,Integer>)args[0];
            //max value = root as lcs for leaves = basis for nominalization
            return result*1.0/Statistics.getMaxJcnValue(freqs);
        } else if (measure.equalsIgnoreCase("hso")) {
            if (Double.compare(result,0) == 0) {
                return result;
            }
            //max value = 15 for strong relations
            return result/15.0;
        } else if (measure.equalsIgnoreCase("lesk")) {
            if (null == args) {
                return -1;
            } //no args given == no value
            if (args.length<1) { //should never happen
                System.out.println("Error: Missing arguments for lesk similarity.");
                System.exit(0);
            }
            if (Double.compare(result,0) == 0) {
                return result;
            }

            //for lesk,  result is simply the overlap.
            //simplified normalization by Verena Henrich:
            int maxFieldSize = (Integer)args[0];
            return result/maxFieldSize;
            
            //older, slower version: 
//            //input: gnet,oneOrthForm,size,limit,hypernymsOnly,includeGloss
//            double maxForTheseSettings = Statistics.getLeskMax(
//                    Relatedness.gnet,(Boolean)args[0], (Integer)args[1],
//                    (Integer)args[2],(Boolean)args[3], (Boolean)args[4]);
//                return (result*1.0/maxForTheseSettings);

//            System.out.println((Double)args[5] + " ### " + (result*1.0/maxForTheseSettings));
//            return (Double)args[5];
        }
        System.out.println("Error: unknown method.");
        return -1;
    }

    /**
     * The return String contains the name of the measure, the result value
     * (not normalized) and the number of arguments.
     * @return a String representation of this RelatednessResult object.
     * @override toString()
     */
    @Override
    public String toString() {
        String s = measure+" value "+result+"; "+args.length+" arguments.";
        return s;
    }

}
