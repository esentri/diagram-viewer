package io.domainlifecycles.diagramviewer.sql;

import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;


@Configuration
@Slf4j
public class SQLDDLGeneratorConfig {

    @Bean
    public SQLDDLGeneratorService sqlddlGeneratorService() {
        var scannedService = scanSQLGeneratorImplementation();
        if (scannedService == null){
            return new NoOpSQLDDLGeneratorService();
        }else{
            return scannedService;
        }

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
