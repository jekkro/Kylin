package com.kylinolap.dict.lookup;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.util.LocalFileMetadataTestCase;
import com.kylinolap.metadata.MetadataManager;
import com.kylinolap.metadata.model.TableDesc;
import org.apache.hadoop.hbase.util.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shaoshi on 2/3/15.
 */
public class LookupTableTest extends LocalFileMetadataTestCase {

    private KylinConfig config = null;

    private LookupTable lookupTable;

    @Before
    public void setup() throws Exception {
        createTestMetadata();
        config = KylinConfig.getInstanceFromEnv();
        lookupTable = initLookupTable();
    }

    @After
    public void tearDown() {
        cleanupTestMetadata();
    }

    @Test
    public void testScan() throws Exception {
        List<String> values = new ArrayList<String>();
        values.add("2012-01-24");
        values.add("2012-12-30");
        List<String> results = lookupTable.scan("CAL_DT", values, "YEAR_BEG_DT");

        Assert.assertTrue(results.size() > 0);
        for (String i : results) {
            System.out.println(i);

            Assert.assertEquals("2012-01-01", i);
        }
    }

    @Test
    public void testMapRange() throws Exception {
        List<String> values = new ArrayList<String>();
        values.add("2012-01-24");
        values.add("2012-12-30");
        Pair<String, String> results = lookupTable.mapRange("CAL_DT", "2012-01-24", "2012-12-30", "QTR_BEG_DT");


        Assert.assertTrue(results != null);
        System.out.println("The first qtr_beg_dt is " + results.getFirst());
        System.out.println("The last qtr_beg_dt is " + results.getSecond());

        Assert.assertEquals("2012-01-01", results.getFirst());
        Assert.assertEquals( "2012-10-01" , results.getSecond());
    }

    @Test
    public void testMapValues() throws Exception {
        Set<String> values = new HashSet<String>();
        values.add("2012-01-24");
        values.add("2012-12-30");
        Set<String> results = lookupTable.mapValues("CAL_DT", values, "YEAR_BEG_DT");

        Assert.assertTrue(results.size() == 1);
        for (String i : results) {
            System.out.println(i);

            Assert.assertEquals("2012-01-01", i);
        }
    }

    public LookupTable initLookupTable() throws Exception {


        MetadataManager metaMgr = MetadataManager.getInstance(config);


        String tableName = "EDW.TEST_CAL_DT";
        String[] pkCols = new String[]{"CAL_DT"};
        String snapshotResPath = "/table_snapshot/TEST_CAL_DT.csv/4af48c94-86de-4e22-a4fd-c49b06cbaa4f.snapshot";
        SnapshotTable snapshot = getSnapshotManager().getSnapshotTable(snapshotResPath);
        TableDesc tableDesc = metaMgr.getTableDesc(tableName);
        LookupTable lt = new LookupStringTable(tableDesc, pkCols, snapshot);

        System.out.println(lt);


        return lt;
    }

    private SnapshotManager getSnapshotManager() {
        return SnapshotManager.getInstance(config);
    }

}