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
 * Same as java.util.HashMap<String,Long>, but allows for a check for existing
 * entries before adding a new key-value pair.
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class LongHashMap extends HashMap<String, Long> {

    /**
     * Puts a new key-value pair into the map; adds value to previous value if
     * entry already exists.
     * @param key The entry to add.
     * @param value The value to add.
     * @return the previous value associated with key, or null if there was no
     *         mapping for key.
     */
    public Long putCumulative(String key, Long value) {
        if (this.containsKey(key)) {
            this.put(key, value+this.get(key));
            return this.get(key);
        }
        else {
            this.put(key, value);
            return null;
        }
    }
}
