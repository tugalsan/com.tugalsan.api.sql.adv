package com.tugalsan.api.sql.adv.server;

import java.util.*;
import java.util.stream.*;
import com.tugalsan.api.cast.client.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.sql.conn.server.*;
import com.tugalsan.api.sql.sanitize.server.*;
import com.tugalsan.api.sql.select.server.*;
import com.tugalsan.api.sql.update.server.*;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;

public class TS_SQLAdvVarUtils {

    final private static TS_Log d = TS_Log.of(TS_SQLAdvVarUtils.class);

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

    public static TGS_UnionExcuseVoid setVariable_ArraySize(TS_SQLConnAnchor anchor, CharSequence atVarName, long size) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("setVariable_ArraySize", "atVarname", atVarName, "size", size);
        return setVariable(anchor, TGS_StringUtils.concat(atVarName, "_size"), size);
    }

    public static TGS_UnionExcuse<Long> getVariable_ArraySize(TS_SQLConnAnchor anchor, CharSequence atVarName) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("getVariable_ArraySize", "atVarname", atVarName);
        var u = getVarible(anchor, TGS_StringUtils.concat(atVarName, "_size"), VARTYP_LNG());
        if (u.isExcuse()) {
            return u.toExcuse();
        }
        var lng = (Long) u.value();
        if (lng < 0) {
            return TGS_UnionExcuse.ofExcuse(d.className, "getVariable_ArraySize", "lng < 0");
        }
        return TGS_UnionExcuse.of(lng);
    }

    public static TGS_UnionExcuseVoid addVariable(TS_SQLConnAnchor anchor, String atVarName, Object value) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("addVariable", "atVarname", atVarName, "value", value);
        var size = getVariable_ArraySize(anchor, atVarName);
        if (size.isExcuse()) {
            return size.toExcuseVoid();
        }
        var u_set = setVariable(anchor, atVarName, size.value(), value);
        if (u_set.isExcuse()) {
            return u_set;
        }
        return setVariable_ArraySize(
                anchor,
                atVarName,
                size.value() + 1);
    }

    public static TGS_UnionExcuseVoid setVariable(TS_SQLConnAnchor anchor, CharSequence atVarName, long listIdx, Object value) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("setVariable", "atVarname", atVarName, "arrayListIdx", listIdx, "value", value);
        if (value instanceof Long || value instanceof String) {
            return setVariable(anchor, TGS_StringUtils.concat(atVarName, "_", String.valueOf(listIdx)), value);
        }
        return TGS_UnionExcuseVoid.ofExcuse(d.className, "setVariable", "value_type is not Long|String");
    }

    public static TGS_UnionExcuseVoid setVariable(TS_SQLConnAnchor anchor, CharSequence atVarName, Object value) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("setVariable", "atVarName", atVarName, "value", value);
        //SELECT @var3 := 4;??
        //SET @var2 := @var1-2;
        if (value == null) {
            return TGS_UnionExcuseVoid.ofExcuse(d.className, "setVariable", "value == null");
        }
        if (List.class.isInstance(value)) {
            d.ci("setVariable", "ArrayList.class.isInstance(value)", "value", value);
            var arrVal = (List) value;
            var u_set = setVariable(anchor, atVarName, "ArrayList");
            if (u_set.isExcuse()) {
                return u_set;
            }
            List<String> arrNames = TGS_ListUtils.of();
            for (var i = 0; i < arrVal.size(); i++) {
                arrNames.add(TGS_StringUtils.concat(atVarName, "_", String.valueOf(i)));
            }
            var u_push = pushVaribles(anchor, arrNames, arrVal);
            if (u_push.isExcuse()) {
                return u_push;
            }
            return setVariable_ArraySize(anchor, atVarName, arrVal.size());
        }
        if (String.class.isInstance(value)) {
            var sql = TGS_StringUtils.concat("SET ", atVarName, " := \"", String.valueOf(value), "\"");
            d.ci("setVariable", "String.class.isInstance(value)", "sql", sql);
            var u_update = TS_SQLUpdateStmtUtils.update(anchor, sql);
            if (u_update.isExcuse()) {
                return u_update.toExcuseVoid();
            }
            return TGS_UnionExcuseVoid.ofVoid();
        }
        if (Integer.class.isInstance(value) || TGS_CastUtils.toLong(value) != null) {
            var sql = TGS_StringUtils.concat("SET ", atVarName, " := ", String.valueOf(value));
            d.ci("setVariable", "Integer.class.isInstance(value) || TK_GWTSharedUtils.cast2Long(value) != null", "sql", sql);
            var u_update = TS_SQLUpdateStmtUtils.update(anchor, sql);
            if (u_update.isExcuse()) {
                return u_update.toExcuseVoid();
            }
            return TGS_UnionExcuseVoid.ofVoid();
        }
        return TGS_UnionExcuseVoid.ofExcuse(d.className, "setVariable", "setVariable.ERROR: Connection.setVariable unknown value instance: " + value);
    }

    public static TGS_UnionExcuse<Object> getVarible(TS_SQLConnAnchor anchor, CharSequence atVarName, int long0_String1_ArrayLong2_ArrayString3) {
        TS_SQLSanitizeUtils.sanitize(atVarName);
        d.ci("getVarible.", "atVarName", atVarName, "long0_String1_ArrayLong2_ArrayString3", long0_String1_ArrayLong2_ArrayString3);
        //SELECT @var1, @var2;
        if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG() || long0_String1_ArrayLong2_ArrayString3 == VARTYP_STR()) {
            var wrap = new Object() {
                TGS_UnionExcuse<Long> id;
                TGS_UnionExcuse<String> value;
                TGS_UnionExcuseVoid walkCells;
                TGS_UnionExcuseVoid walkSelect;
            };
            var sql = "SELECT " + atVarName;
            d.ci("getVarible.INFO: Connection.getVarible.sql: ", sql);
            wrap.walkSelect = TS_SQLSelectStmtUtils.select(anchor, sql, rs -> {
                wrap.walkCells = rs.walkCells(rs0 -> d.ce("getVarible", "empty", rs0.meta.command()), (ri, ci) -> {
                    if (wrap.id == null && wrap.id.isExcuse()) {
                        return;
                    }
                    if (wrap.value == null && wrap.value.isExcuse()) {
                        return;
                    }
                    if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG()) {
                        wrap.id = rs.lng.get(ri, ci);
                    } else {
                        wrap.value = rs.str.get(ri, ci);
                    }
                });
            });
            if (wrap.id != null && wrap.id.isExcuse()) {
                return wrap.id.toExcuse();
            }
            if (wrap.value != null && wrap.value.isExcuse()) {
                return wrap.value.toExcuse();
            }
            if (wrap.walkCells != null && wrap.walkCells.isExcuse()) {
                return wrap.walkCells.toExcuse();
            }
            if (wrap.walkSelect != null && wrap.walkSelect.isExcuse()) {
                return wrap.walkSelect.toExcuse();
            }
            if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_LNG()) {
                return TGS_UnionExcuse.of(wrap.id);
            } else {
                return TGS_UnionExcuse.of(wrap.value);
            }
        } else if (long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_LNG() || long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_STR()) {
            var size = getVariable_ArraySize(anchor, atVarName);
            if (size.isExcuse()) {
                return size.toExcuse();
            }
            List<String> arrNames = TGS_ListUtils.of();
            LongStream.range(0, size.value()).forEachOrdered(L -> arrNames.add(TGS_StringUtils.concat(atVarName, "_", String.valueOf(L))));
            List<Long> arrValues = TGS_ListUtils.of();
            var u_pop = popVaribles(anchor, arrNames, arrValues, long0_String1_ArrayLong2_ArrayString3 == VARTYP_ARR_LNG() ? VARTYP_LNG() : VARTYP_STR());
            if (u_pop.isExcuse()) {
                return u_pop.toExcuse();
            }
            return TGS_UnionExcuse.of(arrValues);
        }
        return TGS_UnionExcuse.ofExcuse(d.className, "getVarible", "unknown VARTYP");
    }

    public static TGS_UnionExcuseVoid pushVaribles(TS_SQLConnAnchor anchor, List<String> atVarNames, List values) {
        TS_SQLSanitizeUtils.sanitize(atVarNames);
        d.ci("pushVaribles.", "atVarNames", atVarNames, "values", values);
        for (var i = 0; i < atVarNames.size(); i++) {
            var u_set = setVariable(anchor, atVarNames.get(i), values.get(i));
            if (u_set.isExcuse()) {
                return u_set;
            }
        }
        return TGS_UnionExcuseVoid.ofVoid();
    }

    public static TGS_UnionExcuseVoid popVaribles(TS_SQLConnAnchor anchor, List<String> atVarNames, List result, int long0_String1) {
        TS_SQLSanitizeUtils.sanitize(atVarNames);
        d.ci("popVaribles.", "atVarNames", atVarNames, "result", result, "long0_String1", long0_String1);
        //SELECT @var1, @var2;
        result.clear();
        var sql = "SELECT " + TGS_StringUtils.toString(atVarNames, ", ");
        d.ci("popVaribles.INFO: Connection.popVaribles.sql: ", sql);
        var wrap = new Object() {
            TGS_UnionExcuse<Long> id;
            TGS_UnionExcuse<String> value;
            TGS_UnionExcuseVoid walkCells;
            TGS_UnionExcuseVoid walkStmt;
        };
        wrap.walkStmt = TS_SQLSelectStmtUtils.select(anchor, sql, rs -> {
            wrap.walkCells = rs.walkCells(rs0 -> {
            }, (ri, ci) -> {
                if (wrap.id != null && wrap.id.isExcuse()) {
                    return;
                }
                if (wrap.value != null && wrap.value.isExcuse()) {
                    return;
                }
                if (long0_String1 == VARTYP_LNG()) {
                    wrap.id = rs.lng.get(ri, ci);
                    if (wrap.id.isExcuse()) {
                        return;
                    }
                    result.add(wrap.id.value());
                } else {
                    wrap.value = rs.str.get(ri, ci);
                    if (wrap.value.isExcuse()) {
                        return;
                    }
                    result.add(wrap.value.value());
                }
            });
        });
        if (wrap.id != null && wrap.id.isExcuse()) {
            return wrap.id.toExcuseVoid();
        }
        if (wrap.value != null && wrap.value.isExcuse()) {
            return wrap.value.toExcuseVoid();
        }
        if (wrap.walkCells != null && wrap.walkCells.isExcuse()) {
            return wrap.walkCells;
        }
        return wrap.walkStmt;
    }
}
