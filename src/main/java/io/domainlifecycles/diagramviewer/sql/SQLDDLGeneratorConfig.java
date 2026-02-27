/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.sql;

import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class SQLDDLGeneratorConfig {

    @Bean
    public SQLDDLGeneratorService sqlddlGeneratorService() {
        var scannedService = scanSQLGeneratorImplementation();
        return Objects.requireNonNullElseGet(scannedService, NoOpSQLDDLGeneratorService::new);
    }

    private SQLDDLGeneratorService scanSQLGeneratorImplementation(){
        var classGraph = new ClassGraph()
                //.verbose()
                .enableAllInfo();

        try (ScanResult scanResult = classGraph.scan()) {  // Start the scan

            var generatorClasses = scanResult.getClassesImplementing(SQLDDLGeneratorService.class)
                    .stream()
                    .filter(c ->
                            !SQLDDLGeneratorService.class.getName().equals(c.getName()) && !NoOpSQLDDLGeneratorService.class.getName().equals(c.getName())
                    )
                    .map(r -> (Class<? extends SQLDDLGeneratorService>) loadClass(r))
                    .toList();
            if(generatorClasses.size()>1) {
                log.error("Multiple SQLDDLGeneratorService implementations found! Falling back to NoOpSQLDDLGeneratorService!");
            }else if(generatorClasses.size()==1){
                return generatorClasses.get(0).getDeclaredConstructor().newInstance();
            }

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error("Instantiating SQLDDLGeneratorService failed!",e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private Class<?> loadClass(ClassInfo classInfo) {
        try {
            return classInfo.loadClass();
        }catch (Throwable t) {
            log.error("Loading class '{}' failed!", classInfo.getName(), t);
        }
        return null;
    }
}
