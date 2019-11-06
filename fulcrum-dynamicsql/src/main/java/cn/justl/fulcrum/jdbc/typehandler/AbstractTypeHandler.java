package cn.justl.fulcrum.jdbc.typehandler;

import cn.justl.fulcrum.data.ValueHolder;
import cn.justl.fulcrum.exceptions.SQLExecuteException;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.util.Set;

/**
 * @Date : 2019/10/25
 * @Author : jingl.wang [jingl.wang123@gmail.com]
 * @Desc :
 * Abstract base class for {@link TypeHandler} implementations.
 *
 * For each subclass, such methods shoule be implemented:
 * {@link AbstractTypeHandler#setNonNullParam} for setting parameters that the return of {@link ValueHolder#getVal()} is not null;
 * {@link AbstractTypeHandler#setNullParam} for setting parameters when the return of {@link ValueHolder#getVal()} is null;
 * {@link AbstractTypeHandler#getSupportTypeNames()} for defining the support type names;
 * {@link AbstractTypeHandler#getSupportTypes()} for defining the support types.
 *
 * @see IntegerTypeHandler
 */
public abstract class AbstractTypeHandler implements TypeHandler {

    @Override
    public void setParam(PreparedStatement ps, int index, ValueHolder valueHolder) throws SQLExecuteException {
        if (valueHolder == null)
            throw new SQLExecuteException("input valueHolder is null");
        if (valueHolder.getVal() != null)
            setNonNullParam(ps, index, valueHolder);
        else
            setNullParam(ps, index, valueHolder);
    }

    @Override
    public boolean isMatch(ValueHolder holder) {
        return isMatchByTypeName(holder) || isMatchByType(holder);
    }

    public boolean isMatchByType(ValueHolder valueHolder) {
        return valueHolder != null
                && valueHolder.getVal() != null
                && getSupportTypes().stream()
                .anyMatch(x -> x.equals(valueHolder.getVal().getClass()));
    }

    public boolean isMatchByTypeName(ValueHolder valueHolder) {
        return valueHolder != null
                && StringUtils.isNotBlank(valueHolder.getType())
                && getSupportTypeNames().stream().anyMatch(x->StringUtils.equalsIgnoreCase(x, valueHolder.getType()));
    }

    /**
     * To set parameters for nonnull value
     * @param ps
     * @param index
     * @param valueHolder
     * @throws SQLExecuteException
     */
    public abstract void setNonNullParam(PreparedStatement ps, int index, ValueHolder valueHolder) throws SQLExecuteException;

    /**
     * To set parameters for null value
     * @param ps
     * @param index
     * @param valueHolder
     * @throws SQLExecuteException
     */
    public abstract void setNullParam(PreparedStatement ps, int index, ValueHolder valueHolder) throws SQLExecuteException;

    /**
     * Get the name of support types
     * Subclass should implement this method to define which set of types can be handled in this class
     * @return a set of the name of support types
     */
    public abstract Set<String> getSupportTypeNames();

    /**
     * Get the type of support types
     * Subclass should implement this method to define which set of types can be handled in this class
     * @return
     */
    public abstract Set<Class> getSupportTypes();

}
