package brix.plugin.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IDetachable;

import brix.jcr.api.JcrNode;
import brix.jcr.api.JcrNodeIterator;
import brix.web.reference.Reference;

public class Menu implements IDetachable
{

    public static class Entry implements IDetachable
    {
        private final Entry parent;

        public Entry(Entry parent)
        {
            this.parent = parent;
        }

        public Entry getParent()
        {
            return parent;
        }

        private final List<ChildEntry> children = new ArrayList<ChildEntry>();

        public List<ChildEntry> getChildren()
        {
            return children;
        }

        public void detach()
        {
            for (ChildEntry entry : children)
            {
                entry.detach();
            }
        }
    };

    public static class RootEntry extends Entry
    {
        public RootEntry()
        {
            super(null);
        }

        @Override
        public String toString()
        {
            return "Menu Root";
        }
    };

    public static class ChildEntry extends Entry
    {

        public ChildEntry(Entry parent)
        {
            super(parent);
        }

        private String title;
        private Reference reference;

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public Reference getReference()
        {
            return reference;
        }

        public void setReference(Reference reference)
        {
            this.reference = reference;
        }

        @Override
        public void detach()
        {
            super.detach();
            if (reference != null)
            {
                reference.detach();
            }
        }

        @Override
        public String toString()
        {
            return getTitle();
        }
    }

    private RootEntry root = new RootEntry();

    public RootEntry getRoot()
    {
        return root;
    }

    public void detach()
    {
        root.detach();
    }

    private String name;

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    private void saveEntry(JcrNode node, Entry entry)
    {
        if (entry instanceof ChildEntry)
        {
            ChildEntry childEntry = (ChildEntry)entry;
            node.setProperty("title", childEntry.getTitle());
            if (childEntry.getReference() != null)
            {
                childEntry.getReference().save(node, "reference");
            }
        }
        for (Entry e : entry.getChildren())
        {
            JcrNode child = node.addNode("child");
            saveEntry(child, e);
        }
    }

    public void save(JcrNode node)
    {
        if (!node.isNodeType("mix:referenceable"))
        {
            node.addMixin("mix:referenceable");
        }
        node.setProperty("name", getName());
        if (node.hasNode("menu"))
        {
            node.getNode("menu").remove();
        }
        JcrNode menu = node.addNode("menu", "nt:unstructured");
        saveEntry(menu, getRoot());
    }

    public void loadName(JcrNode node)
    {
        if (node.hasProperty("name"))
        {
            setName(node.getProperty("name").getString());
        }
    }

    private void loadChildEntry(JcrNode node, ChildEntry entry)
    {
        if (node.hasProperty("title"))
        {
            entry.setTitle(node.getProperty("title").getString());
        }
        entry.setReference(Reference.load(node, "reference"));
    }

    private void loadEntry(JcrNode node, Entry entry)
    {
        JcrNodeIterator i = node.getNodes("child");
        while (i.hasNext())
        {
            JcrNode child = i.nextNode();
            ChildEntry e = new ChildEntry(entry);
            loadChildEntry(child, e);
            loadEntry(child, e);
            entry.getChildren().add(e);
        }
    }

    public void loadMenu(JcrNode node)
    {
        root = new RootEntry();
        if (node.hasNode("menu"))
        {
            loadEntry(node.getNode("menu"), root);
        }
    }

    public void load(JcrNode node)
    {
        loadName(node);
        loadMenu(node);
    }

}