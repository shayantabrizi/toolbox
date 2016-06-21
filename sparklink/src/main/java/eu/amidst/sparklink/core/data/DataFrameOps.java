package eu.amidst.sparklink.core.data;

import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataOnMemory;
import eu.amidst.core.datastream.Attribute;
import eu.amidst.core.datastream.Attributes;
import eu.amidst.core.datastream.filereaders.DataInstanceFromDataRow;
import eu.amidst.core.variables.StateSpaceType;
import eu.amidst.core.variables.stateSpaceTypes.FiniteStateSpace;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jarias on 21/06/16.
 */
class DataFrameOps {


    static JavaRDD<DataInstance> toDataInstanceRDD(DataFrame data, Attributes attributes) {

        JavaRDD<double[]> rawRDD = data.rdd()
                                  .toJavaRDD()
                                  .map( row -> transformRow2DataInstance(row, attributes) );

        return rawRDD.map(v ->  new DataInstanceFromDataRow( new DataRowSpark(v, attributes) ) );
    }


    static JavaRDD<DataOnMemory<DataInstance>> toBatchedRDD(JavaRDD<DataInstance> instanceRDD,
                                                            Attributes attributes, int batchSize) {

        return instanceRDD.mapPartitions( partition -> partition2Batches(partition, attributes, batchSize) );
    }


    private static double[] transformRow2DataInstance(Row row, Attributes attributes) throws Exception {

        double[] instance = new double[row.length()];

        for (int i = 0; i < row.length(); i++) {

            Attribute att = attributes.getFullListOfAttributes().get(i);
            StateSpaceType space = att.getStateSpaceType();

            switch (space.getStateSpaceTypeEnum()) {
                case REAL:
                    instance[i] = row.getDouble(i);
                    break;

                case FINITE_SET:
                    String state = row.getString(i);
                    double index = ((FiniteStateSpace) space).getIndexOfState(state);
                    instance[i] = index;
                    break;

                default:
                    // This should never execute
                    throw new Exception("Unrecognized Error");
            }
        }

        return instance;
    }


    private static Iterable<DataOnMemory<DataInstance>> partition2Batches(Iterator<DataInstance> partition,
                                                            Attributes attributes, int batchSize) {

        ArrayList<DataOnMemory<DataInstance>> batches = new ArrayList<>();

        int currentSize = 0;
        ArrayList<DataInstance> batch = new ArrayList<>();

        while(partition.hasNext()) {

            batch.add(partition.next());
            currentSize++;

            if (currentSize >= batchSize) {
                currentSize = 0;
                batches.add(new DataOnMemoryListContainerSerializable<DataInstance>(attributes, batch));
                batch = new ArrayList<>();
            }

        }

        // Add the last batch if there are any remaining instances:
        if (currentSize > 0)
            batches.add(new DataOnMemoryListContainerSerializable<>(attributes, batch));

        return batches;
    }

}
