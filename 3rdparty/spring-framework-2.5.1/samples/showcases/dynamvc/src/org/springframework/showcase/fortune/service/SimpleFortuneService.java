/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.showcase.fortune.service;

import org.springframework.showcase.fortune.domain.Fortune;

import java.util.*;

/**
 * A simple {@link FortuneService} implementation.
 *
 * @author Rick Evans
 */
public class SimpleFortuneService implements FortuneService {

    private Map fortunes;


    public void setFortunes(Set fortunes) {
        this.fortunes = new HashMap();
        if (fortunes != null) {
            int index = 0;
            for (Iterator it = fortunes.iterator(); it.hasNext(); ++index) {
                Fortune fortune = (Fortune) it.next();
                this.fortunes.put(new Integer(index), fortune);
            }
        }
    }


    public Fortune tellFortune() {
        Fortune fortune;
        if (this.fortunes.size() == 0) {
            fortune = createDefaultFortune();
        } else {
            Integer indexKey = new Integer(new Random().nextInt(this.fortunes.size()));
            if (this.fortunes.containsKey(indexKey)) {
                fortune = (Fortune) this.fortunes.get(indexKey);
            } else {
                fortune = createDefaultFortune();
            }
        }
        return fortune;
    }

    private Fortune createDefaultFortune() {
        Fortune fortune = new Fortune();
        fortune.setFortune("The crystal ball is cloudy today. Cross my plam with silver to part the clouds.");
        fortune.setSource("Madame Penc");
        return fortune;
    }

}
