package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.starlettech.enums.DataFormat;

/**
 * Annotation for data-driven test configuration
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataDriven {
    /**
     * Data source file path
     */
    String dataSource() default "";
    
    /**
     * Data provider method name
     */
    String dataProvider() default "";
    
    /**
     * Data format type
     */
    DataFormat format() default DataFormat.JSON;
    
    /**
     * Test data key/section
     */
    String dataKey() default "";
    
    /**
     * Whether to run tests in parallel
     */
    boolean parallel() default false;
    
    /**
     * Maximum number of data sets to process
     */
    int maxDataSets() default Integer.MAX_VALUE;
    
    
}