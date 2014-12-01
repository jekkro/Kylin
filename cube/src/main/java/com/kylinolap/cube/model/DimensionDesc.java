/*
 * Copyright 2013-2014 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kylinolap.cube.model;

import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kylinolap.common.util.StringUtil;
import com.kylinolap.metadata.model.JoinDesc;
import com.kylinolap.metadata.model.LookupDesc;
import com.kylinolap.metadata.model.TableDesc;
import com.kylinolap.metadata.model.realization.TblColRef;

/**
 * Created with IntelliJ IDEA. User: lukhan Date: 9/24/13 Time: 10:40 AM To
 * change this template use File | Settings | File Templates.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DimensionDesc {

    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    private JoinDesc join;
    @JsonProperty("hierarchy")
    private HierarchyDesc[] hierarchy;
    private String table;
    @JsonProperty("column")
    private String column;
    @JsonProperty("derived")
    private String[] derived;

    // computed
    private TblColRef[] columnRefs;
    private TblColRef[] derivedColRefs;

    public boolean isHierarchyColumn(TblColRef col) {
        if (hierarchy == null)
            return false;

        for (HierarchyDesc hier : hierarchy) {
            if (hier.getColumnRef().equals(col))
                return true;
        }
        return false;
    }


    public String getTable() {
        return table.toUpperCase();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JoinDesc getJoin() {
        return join;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TblColRef[] getColumnRefs() {
        return this.columnRefs;
    }

    public void setColumnRefs(TblColRef[] colRefs) {
        this.columnRefs = colRefs;
    }

    public String getColumn() {
        return this.column;
    }

    public void setColumn(String column) {
        this.column = column;
        if (this.column != null)
            this.column = this.column.toUpperCase();
    }

    public HierarchyDesc[] getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(HierarchyDesc[] hierarchy) {
        this.hierarchy = hierarchy;
    }

    public String[] getDerived() {
        return derived;
    }

    public void setDerived(String[] derived) {
        this.derived = derived;
    }

    public TblColRef[] getDerivedColRefs() {
        return derivedColRefs;
    }

    public void setDerivedColRefs(TblColRef[] derivedColRefs) {
        this.derivedColRefs = derivedColRefs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DimensionDesc that = (DimensionDesc) o;

        if (id != that.id)
            return false;
        if (!name.equals(that.name))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DimensionDesc [name=" + name + ", join=" + join + ", hierarchy=" + Arrays.toString(hierarchy) + ", table=" + table + ", column=" + column + ", derived=" + Arrays.toString(derived) + "]";
    }

    public void init(CubeDesc cubeDesc, Map<String, TableDesc> tables) {
        if (name != null)
            name = name.toUpperCase();
        if (column != null)
            column = column.toUpperCase();
        
        // parse 'column' to get the table name; if table name is not appeared, use fact table;
        String[] splits = this.column.split(".");
        if (splits.length > 1) {
            table = splits[splits.length - 2].toUpperCase();
        } else {
            table = cubeDesc.getFactTable().toUpperCase();
        }

        TableDesc tableDesc = tables.get(table);
        if (tableDesc == null)
            throw new IllegalStateException("Can't find table " + table + " on dimension " + name);

        boolean isOnFactTable = table.equalsIgnoreCase(cubeDesc.getFactTable());

        if (hierarchy != null && hierarchy.length == 0)
            hierarchy = null;
        if (derived != null && derived.length == 0)
            derived = null;
        
        if(!isOnFactTable) {
            for(LookupDesc lookup: cubeDesc.getModel().getLookups()) {
                if(lookup.getTable().equals(table)) {
                    join = lookup.getJoin();
                    break;
                }
            }
        }

        if (join != null) {
            StringUtil.toUpperCaseArray(join.getForeignKey(), join.getForeignKey());
            StringUtil.toUpperCaseArray(join.getPrimaryKey(), join.getPrimaryKey());
        }

        if (hierarchy != null) {
            for (HierarchyDesc h : hierarchy)
                h.setColumn(h.getColumn().toUpperCase());
        }

        if (derived != null) {
            StringUtil.toUpperCaseArray(derived, derived);
        }
    }

}