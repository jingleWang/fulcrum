package cn.justl.fulcrum.scripthandler.handlers;

import cn.justl.fulcrum.contexts.ExecuteContext;
import cn.justl.fulcrum.contexts.ScriptContext;
import cn.justl.fulcrum.exceptions.ScriptFailedException;
import cn.justl.fulcrum.scripthandler.ScriptResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @Date : 2019/9/29
 * @Author : jingl.wang [jingl.wang123@gmail.com]
 * @Desc :
 * an implement of ScriptHandler to process foreach script
 */
public final class ForeachScriptHandler extends AbstractScriptHandler {

    private final String collectionName;
    private final String itemName;
    private final String indexName;
    private final String separator;

    public ForeachScriptHandler(String collectionName, String itemName, String indexName, String separator) {
        this.collectionName = collectionName;
        this.itemName = itemName;
        this.indexName = indexName;
        this.separator = separator;
    }

    @Override
    public ScriptResult process(ScriptContext context) throws ScriptFailedException {
        Object collection = validate(context);
         if (collection instanceof Map) {
            return processMap(context, (Map) collection);
        } else if (collection instanceof Collection) {
            return processCollection(context, (Collection) collection);
        } else {
            throw new ScriptFailedException("The type of <" + collectionName + "> can not be used" + " in foreach script, expected Map or Collection!");
        }
    }

    private ScriptResult processMap(ScriptContext context, Map target) throws ScriptFailedException {
        return doProcess(context, target.entrySet().toArray());
    }

    private ScriptResult processCollection(ScriptContext context, Collection target) throws ScriptFailedException {
        return doProcess(context, target.toArray());
    }

    private ScriptResult doProcess(ScriptContext context, Object[] items) throws ScriptFailedException {
        ScriptResult sr = new ScriptResult();
        sr.setSql(new StringBuilder());
        boolean needSeparator = false;
        for (int i = 0; i < items.length; i++) {
            if (null == items[i]) continue;
            try {
                prepareParam(context, items[i], i);
                ScriptResult subRs = getChild().process(context);

                if (subRs == null || subRs instanceof ScriptResult.EmptyScriptResult || StringUtils.isBlank(subRs.getSql())) continue;

                if (needSeparator && StringUtils.isNotEmpty(separator))
                    sr.getSql().append(separator);
                else if (!needSeparator)
                    needSeparator = true;
                sr.getSql().append(subRs.getSql());
                sr.addAllValue(subRs.getValueHolders());
            } finally {
                clearParam(context);
            }
        }
        return sr;
    }

    private Object validate(ScriptContext context) throws ScriptFailedException {
        if (getChild() == null) {
            throw new ScriptFailedException("No child ScriptHandler exist in execution tree!");
        }

        if (!context.containsParam(collectionName))
            throw new ScriptFailedException("The param <" + collectionName + "> can not be " + "founded!");

        Object collection;
        if ((collection = context.getParam(collectionName)) == null)
            throw new ScriptFailedException("The param <" + collectionName + "> must not be Null!");

        return collection;
    }

    private void prepareParam(ScriptContext context, Object item, int index) {
        context.prepareTempParamsContext();
        context.addTempParam(itemName, item);
        Optional.ofNullable(indexName).ifPresent(x -> {
            context.addTempParam(x, index);
        });
    }

    private void clearParam(ScriptContext context) {
        context.popTempParams();
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getItemName() {
        return itemName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getSeparator() {
        return separator;
    }

    @Override
    public String toString() {
        return "ForeachScriptHandler{" + "collectionName='" + collectionName + '\'' + ", itemName='" + itemName + '\'' + ", indexName='" + indexName + '\'' + ", separator='" + separator + '\'' + ", child=" + child + '}';
    }
}
