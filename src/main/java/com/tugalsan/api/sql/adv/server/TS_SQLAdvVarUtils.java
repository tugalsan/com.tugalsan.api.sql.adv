package com.tugalsan.api.sql.adv.server;

import java.util.*;
import java.util.stream.*;
import com.tugalsan.api.cast.client.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.pack.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.sql.conn.server.*;
import com.tugalsan.api.sql.sanitize.server.*;
import com.tugalsan.api.sql.select.server.*;
import com.tugalsan.api.sql.update.server.*;
import com.tugalsan.api.unsafe.client.*;

public class TS_SQLAdvVarUtils {

    final private static TS_Log d = TS_Log.of(TS_SQLAdvVarUtils.class.getSimpleName());

    public static int VARTYP_LNG() {
        return 0;
    }

    public static int VARTYP_STR() {
        return 1;
    }

    public static int VARTYP_ARR_LNG() {
        return 2;
    }

    public static int VARTYP_ARR_STR() {
        return 3;
    }

    public static boolean setVariable_ArraySize(TS_SQLConnAnchor anchor, CharSequence atVarName, long size) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("setVariable_ArraySize", "atVarname", atVarName, "size", size);
        return setVariable(anchor, TGS_StringUtils.concat(atVarName, "_size"), size);
    }

    public static Long getVariable_ArraySize(TS_SQLConnAnchor anchor, CharSequence atVarName) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("getVariable_ArraySize", "atVarname", atVarName);
        Object o = getVarible(anchor, TGS_StringUtils.concat(atVarName, "_size"), VARTYP_LNG());
        return o == null ? null : (Long) o;
    }

    public static boolean addVariable(TS_SQLConnAnchor anchor, String atVarName, Object value) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("addVariable", "atVarname", atVarName, "value", value);
        var size = getVariable_ArraySize(anchor, atVarName);
        if (size != null) {
            if (setVariable(anchor, atVarName, size, value)) {
                return setVariable_ArraySize(anchor, atVarName, ++size);
            }
        }
        return false;
    }

    public static boolean setVariable(TS_SQLConnAnchor anchor, CharSequence atVarName, long listIdx, Object value) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("setVariable", "atVarname", atVarName, "arrayListIdx", listIdx, "value", value);
        return value instanceof Long || value instanceof String ? setVariable(anchor, TGS_StringUtils.concat(atVarName, "_", String.valueOf(listIdx)), value) : false;
    }

    public static boolean setVariable(TS_SQLConnAnchor anchor, CharSequence atVarName, Object value) {
        return TGS_UnSafe.compile(() -> {
            TS_SQLSanitizeUtils.sanitize(atVarName);
            d.ci("setVariable", "atVarName", atVarName, "value", value);
            //SELECT @var3 := 4;??
            //SET @var2 := @var1-2;
            var sql = "";
            if (value == null) {
                d.ce("setVariable", "value == null");
                return false;
            }
            if (List.class.isInstance(value)) {
                d.ci("setVariable", "ArrayList.class.isInstance(value)", "value", value);
                var arrVal = (List) value;

                if (setVariable(anchor, atVarName, "ArrayList")) {
                    List<String> arrNames = TGS_ListUtils.of();
                    for (var i = 0; i < arrVal.size(); i++) {
                        arrNames.add(TGS_StringUtils.concat(atVarName, "_", String.valueOf(i)));
                    }
                    if (pushVaribles(anchor, arrNames, arrVal)) {
                        return setVariable_ArraySize(anchor, atVarName, arrVal.size());
                    }
                }
            }
            if (String.class.isInstance(value)) {
                sql = TGS_StringUtils.concat("SET ", atVarName, " := \"", String.valueOf(value), "\"");
                d.ci("setVariable", "String.class.isInstance(value)", "sql", sql);
                TS_SQLUpdateStmtUtils.update(anchor, sql);

                return true;
            }
            if (Integer.class.isInstance(value) || TGS_CastUtils.toLong(value) != null) {
                sql = TGS_StringUtils.concat("SET ", atVarName, " := ", String.valueOf(value));
                d.ci("setVariable", "Integer.class.isInstance(value) || TK_GWTSharedUtils.cast2Long(value) != null", "sql", sql);
                TS_SQLUpdateStmtUtils.update(anchor, sql);

                return true;
            }
            d.ce("setVariable.ERROR: Connection.setVariable unknown value instance: ", value);
            return false;
        });
    }

    public static Object getVarible(TS_SQLConnAnchor anchor, CharSequence atVarName, int long0_String1_ArrayLong2_ArrayString3) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("getVarible.", "atVarName", atVarName, "long0_String1_ArrayLong2_ArrayString3", long0_String1_ArrayLong2_ArrayString3);
        //SELECT @var1, @var2;
        if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG() || long0_String1_ArrayLong2_ArrayString3 == VARTYP_STR()) {
            TGS_Pack2<Long, String> value = new TGS_Pack2();
            var sql = "SELECT " + atVarName;
            d.ci("getVarible.INFO: Connection.getVarible.sql: ", sql);
            TS_SQLSelectStmtUtils.select(anchor, sql, rs -> {
                rs.walkCells(rs0 -> d.ce("getVarible", "empty", rs0.meta.command()), (ri, ci) -> {
                    if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG()) {
                        value.value0 = rs.lng.get(ri, ci);
                    } else {
                        value.value1 = rs.str.get(ri, ci);
                    }
                });
            });
            if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG()) {
                return value.value0;
            } else {
                return value.value1;
            }
        } else if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_LNG() || long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_STR()) {
            var size = getVariable_ArraySize(anchor, atVarName);
            if (size == null || size < 0) {
                return null;
            }
            List<String> arrNames = TGS_ListUtils.of();
            LongStream.range(0, size).forEachOrdered(L -> arrNames.add(TGS_StringUtils.concat(atVarName, "_", String.valueOf(L))));
            List<Long> arrValues = TGS_ListUtils.of();
            if (!popVaribles(anchor, arrNames, arrValues, long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_LNG() ? VARTYP_LNG() : VARTYP_STR())) {
                return null;
            }
            return arrValues;
        }
        return null;
    }

    public static boolean pushVaribles(TS_SQLConnAnchor anchor, List<String> atVarNames, List values) {
        TS_SQLSanitizeUtils.sanitize(atVarNames);
        d.ci("pushVaribles.", "atVarNames", atVarNames, "values", values);
        for (var i = 0; i < atVarNames.size(); i++) {
            if (!setVariable(anchor, atVarNames.get(i), values.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean popVaribles(TS_SQLConnAnchor anchor, List<String> atVarNames, List result, int long0_String1) {
        TS_SQLSanitizeUtils.sanitize(atVarNames);
        d.ci("popVaribles.", "atVarNames", atVarNames, "result", result, "long0_String1", long0_String1);
        //SELECT @var1, @var2;
        result.clear();
        var sql = "SELECT " + TGS_StringUtils.toString(atVarNames, ", ");
        d.ci("popVaribles.INFO: Connection.popVaribles.sql: ", sql);
        TGS_Pack1<Boolean> r = new TGS_Pack1(false);
        TS_SQLSelectStmtUtils.select(anchor, sql, rs -> {
            rs.walkCells(rs0 -> r.value0 = true, (ri, ci) -> {
                if (long0_String1 == VARTYP_LNG()) {
                    result.add(rs.lng.get(ri, ci));
                } else {
                    result.add(rs.str.get(ri, ci));
                }
            });
            r.value0 = true;
        });
        return r.value0;
    }
}
