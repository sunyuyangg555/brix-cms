package brix.jcr.model.adapter;

import org.apache.wicket.util.convert.ConversionException;

import brix.jcr.api.JcrNode;
import brix.jcr.api.JcrProperty;
import brix.jcr.wrapper.BrixNode;

class NodePathPropertyAdapter extends BasePropertyAdapter
{

    public NodePathPropertyAdapter(String propertyName, String defaultValue, NodeAdapter nodeAdapter)
    {
        super(propertyName, defaultValue, nodeAdapter);
    }

    @Override
    protected Object extractValue(JcrProperty property)
    {
        return ((BrixNode)getNode()).pathForNode(property.getNode());
    }

    @Override
    protected void setProperty(JcrNode node, String propertyName, Object value)
    {
        JcrNode nodeOnPath = ((BrixNode)node).nodeForPath((String)value);

        if (nodeOnPath == null)
        {
            throw new ConversionException("No node found on path '" + value.toString() + "'.");
        }

        node.setProperty(propertyName, nodeOnPath);
    }

}