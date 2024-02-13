package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.*;
import adaa.analytics.rules.rm.operator.Annotations;
import adaa.analytics.rules.rm.tools.Tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class AbstractExampleSet implements IExampleSet {
    private static final long serialVersionUID = 8596141056047402798L;
    private final Map<String, List<IStatistics>> statisticsMap = new HashMap();
    private Map<Double, int[]> idMap = new HashMap();

    private Annotations annotations = new Annotations();

    public AbstractExampleSet() {
    }

    public String getName() {
        return "ExampleSet";
    }

    public Example getExampleFromId(double id) {
        int[] indices = (int[])this.idMap.get(id);
        return indices != null && indices.length > 0 ? this.getExample(indices[0]) : null;
    }

    public int[] getExampleIndicesFromId(double id) {
        return (int[])this.idMap.get(id);
    }

    public String toString() {
        StringBuffer str = new StringBuffer(this.getClass().getSimpleName() + ":" + Tools.getLineSeparator());
        str.append(this.size() + " examples," + Tools.getLineSeparator());
        str.append(this.getAttributes().size() + " regular attributes," + Tools.getLineSeparator());
        boolean first = true;
        Iterator<AttributeRole> s = this.getAttributes().specialAttributes();

        while(s.hasNext()) {
            if (first) {
                str.append("special attributes = {" + Tools.getLineSeparator());
                first = false;
            }

            AttributeRole special = (AttributeRole)s.next();
            str.append("    " + special.getSpecialName() + " = " + special.getAttribute() + Tools.getLineSeparator());
        }

        if (!first) {
            str.append("}");
        } else {
            str.append("no special attributes" + Tools.getLineSeparator());
        }

        return str.toString();
    }

//    public IDataTable createDataTable(IOContainer container) {
//        AttributeWeights weights = null;
//        if (container != null) {
//            try {
//                weights = (AttributeWeights)container.get(AttributeWeights.class);
//                Iterator var3 = this.getAttributes().iterator();
//
//                while(var3.hasNext()) {
//                    Attribute attribute = (Attribute)var3.next();
//                    double weight = weights.getWeight(attribute.getName());
//                    if (Double.isNaN(weight)) {
//                        weights = null;
//                        break;
//                    }
//                }
//            } catch (MissingIOObjectException var7) {
//            }
//        }
//
//        return new DataTableExampleSetAdapter(this, weights);
//    }
//
//    public void writeDataFile(File dataFile, int fractionDigits, boolean quoteNominal, boolean zipped, boolean append, Charset encoding) throws IOException {
//        OutputStream outStream = new FileOutputStream(dataFile, append);
//        Throwable var8 = null;
//
//        try {
//            OutputStream zippedStream = zipped ? new GZIPOutputStream(outStream) : null;
//            Throwable var10 = null;
//
//            try {
//                OutputStreamWriter osw = new OutputStreamWriter((OutputStream)(zipped ? zippedStream : outStream), encoding);
//                Throwable var12 = null;
//
//                try {
//                    PrintWriter out = new PrintWriter(osw);
//                    Throwable var14 = null;
//
//                    try {
//                        Iterator<Example> reader = this.iterator();
//
//                        while(reader.hasNext()) {
//                            out.println(((Example)reader.next()).toDenseString(fractionDigits, quoteNominal));
//                        }
//                    } catch (Throwable var86) {
//                        var14 = var86;
//                        throw var86;
//                    } finally {
//                        if (out != null) {
//                            if (var14 != null) {
//                                try {
//                                    out.close();
//                                } catch (Throwable var85) {
//                                    var14.addSuppressed(var85);
//                                }
//                            } else {
//                                out.close();
//                            }
//                        }
//
//                    }
//                } catch (Throwable var88) {
//                    var12 = var88;
//                    throw var88;
//                } finally {
//                    if (osw != null) {
//                        if (var12 != null) {
//                            try {
//                                osw.close();
//                            } catch (Throwable var84) {
//                                var12.addSuppressed(var84);
//                            }
//                        } else {
//                            osw.close();
//                        }
//                    }
//
//                }
//            } catch (Throwable var90) {
//                var10 = var90;
//                throw var90;
//            } finally {
//                if (zippedStream != null) {
//                    if (var10 != null) {
//                        try {
//                            zippedStream.close();
//                        } catch (Throwable var83) {
//                            var10.addSuppressed(var83);
//                        }
//                    } else {
//                        zippedStream.close();
//                    }
//                }
//
//            }
//        } catch (Throwable var92) {
//            var8 = var92;
//            throw var92;
//        } finally {
//            if (outStream != null) {
//                if (var8 != null) {
//                    try {
//                        outStream.close();
//                    } catch (Throwable var82) {
//                        var8.addSuppressed(var82);
//                    }
//                } else {
//                    outStream.close();
//                }
//            }
//
//        }
//
//    }
//
//    public void writeSparseDataFile(File dataFile, int format, int fractionDigits, boolean quoteNominal, boolean zipped, boolean append, Charset encoding) throws IOException {
//        OutputStream outStream = new FileOutputStream(dataFile, append);
//        Throwable var9 = null;
//
//        try {
//            OutputStream zippedStream = zipped ? new GZIPOutputStream(outStream) : null;
//            Throwable var11 = null;
//
//            try {
//                OutputStreamWriter osw = new OutputStreamWriter((OutputStream)(zipped ? zippedStream : outStream), encoding);
//                Throwable var13 = null;
//
//                try {
//                    PrintWriter out = new PrintWriter(osw);
//                    Throwable var15 = null;
//
//                    try {
//                        Iterator<Example> reader = this.iterator();
//
//                        while(reader.hasNext()) {
//                            out.println(((Example)reader.next()).toSparseString(format, fractionDigits, quoteNominal));
//                        }
//                    } catch (Throwable var87) {
//                        var15 = var87;
//                        throw var87;
//                    } finally {
//                        if (out != null) {
//                            if (var15 != null) {
//                                try {
//                                    out.close();
//                                } catch (Throwable var86) {
//                                    var15.addSuppressed(var86);
//                                }
//                            } else {
//                                out.close();
//                            }
//                        }
//
//                    }
//                } catch (Throwable var89) {
//                    var13 = var89;
//                    throw var89;
//                } finally {
//                    if (osw != null) {
//                        if (var13 != null) {
//                            try {
//                                osw.close();
//                            } catch (Throwable var85) {
//                                var13.addSuppressed(var85);
//                            }
//                        } else {
//                            osw.close();
//                        }
//                    }
//
//                }
//            } catch (Throwable var91) {
//                var11 = var91;
//                throw var91;
//            } finally {
//                if (zippedStream != null) {
//                    if (var11 != null) {
//                        try {
//                            zippedStream.close();
//                        } catch (Throwable var84) {
//                            var11.addSuppressed(var84);
//                        }
//                    } else {
//                        zippedStream.close();
//                    }
//                }
//
//            }
//        } catch (Throwable var93) {
//            var9 = var93;
//            throw var93;
//        } finally {
//            if (outStream != null) {
//                if (var9 != null) {
//                    try {
//                        outStream.close();
//                    } catch (Throwable var83) {
//                        var9.addSuppressed(var83);
//                    }
//                } else {
//                    outStream.close();
//                }
//            }
//
//        }
//
//    }
//
//    public void writeAttributeFile(File attFile, File dataFile, Charset encoding) throws IOException {
//        if (dataFile == null) {
//            throw new IOException("ExampleSet writing: cannot determine path to data file: data file was not given!");
//        } else {
//            String relativePath = Tools.getRelativePath(dataFile, attFile);
//
//            try {
//                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//                Element root = document.createElement("attributeset");
//                root.setAttribute("default_source", relativePath);
//                root.setAttribute("encoding", encoding.name());
//                document.appendChild(root);
//                int sourcecol = 1;
//
//                for(Iterator<AttributeRole> i = this.getAttributes().allAttributeRoles(); i.hasNext(); ++sourcecol) {
//                    root.appendChild(this.writeAttributeMetaData((AttributeRole)i.next(), sourcecol, document, false));
//                }
//
//                FileOutputStream fos = new FileOutputStream(attFile);
//                Throwable var10 = null;
//
//                try {
//                    OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
//                    Throwable var12 = null;
//
//                    try {
//                        PrintWriter writer = new PrintWriter(osw);
//                        Throwable var14 = null;
//
//                        try {
//                            writer.print(XMLTools.toString(document, encoding));
//                        } catch (Throwable var64) {
//                            var14 = var64;
//                            throw var64;
//                        } finally {
//                            if (writer != null) {
//                                if (var14 != null) {
//                                    try {
//                                        writer.close();
//                                    } catch (Throwable var63) {
//                                        var14.addSuppressed(var63);
//                                    }
//                                } else {
//                                    writer.close();
//                                }
//                            }
//
//                        }
//                    } catch (Throwable var66) {
//                        var12 = var66;
//                        throw var66;
//                    } finally {
//                        if (osw != null) {
//                            if (var12 != null) {
//                                try {
//                                    osw.close();
//                                } catch (Throwable var62) {
//                                    var12.addSuppressed(var62);
//                                }
//                            } else {
//                                osw.close();
//                            }
//                        }
//
//                    }
//                } catch (Throwable var68) {
//                    var10 = var68;
//                    throw var68;
//                } finally {
//                    if (fos != null) {
//                        if (var10 != null) {
//                            try {
//                                fos.close();
//                            } catch (Throwable var61) {
//                                var10.addSuppressed(var61);
//                            }
//                        } else {
//                            fos.close();
//                        }
//                    }
//
//                }
//
//            } catch (ParserConfigurationException var70) {
//                throw new IOException("Cannot create XML document builder: " + var70, var70);
//            } catch (XMLException var71) {
//                throw new IOException("Could not format XML document:" + var71, var71);
//            }
//        }
//    }
//
//    public void writeSparseAttributeFile(File attFile, File dataFile, int format, Charset encoding) throws IOException {
//        if (dataFile == null) {
//            throw new IOException("ExampleSet sparse writing: cannot determine path to data file: data file was not given!");
//        } else {
//            String relativePath = Tools.getRelativePath(dataFile, attFile);
//
//            try {
//                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//                Element root = document.createElement("attributeset");
//                root.setAttribute("default_source", relativePath);
//                root.setAttribute("encoding", encoding.name());
//                document.appendChild(root);
//                AttributeRole labelRole = this.getAttributes().getRole("label");
//                if (labelRole != null && format != 4) {
//                    root.appendChild(this.writeAttributeMetaData(labelRole, 0, document, true));
//                }
//
//                AttributeRole idRole = this.getAttributes().getRole("id");
//                if (idRole != null) {
//                    root.appendChild(this.writeAttributeMetaData(idRole, 0, document, true));
//                }
//
//                AttributeRole weightRole = this.getAttributes().getRole("weight");
//                if (weightRole != null) {
//                    root.appendChild(this.writeAttributeMetaData(weightRole, 0, document, true));
//                }
//
//                int sourcecol = 1;
//
//                for(Iterator var12 = this.getAttributes().iterator(); var12.hasNext(); ++sourcecol) {
//                    Attribute attribute = (Attribute)var12.next();
//                    root.appendChild(this.writeAttributeMetaData("attribute", attribute, sourcecol, document, false));
//                }
//
//                FileOutputStream fos = new FileOutputStream(attFile);
//                Throwable var76 = null;
//
//                try {
//                    OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
//                    Throwable var15 = null;
//
//                    try {
//                        PrintWriter writer = new PrintWriter(osw);
//                        Throwable var17 = null;
//
//                        try {
//                            writer.print(XMLTools.toString(document, encoding));
//                        } catch (Throwable var67) {
//                            var17 = var67;
//                            throw var67;
//                        } finally {
//                            if (writer != null) {
//                                if (var17 != null) {
//                                    try {
//                                        writer.close();
//                                    } catch (Throwable var66) {
//                                        var17.addSuppressed(var66);
//                                    }
//                                } else {
//                                    writer.close();
//                                }
//                            }
//
//                        }
//                    } catch (Throwable var69) {
//                        var15 = var69;
//                        throw var69;
//                    } finally {
//                        if (osw != null) {
//                            if (var15 != null) {
//                                try {
//                                    osw.close();
//                                } catch (Throwable var65) {
//                                    var15.addSuppressed(var65);
//                                }
//                            } else {
//                                osw.close();
//                            }
//                        }
//
//                    }
//                } catch (Throwable var71) {
//                    var76 = var71;
//                    throw var71;
//                } finally {
//                    if (fos != null) {
//                        if (var76 != null) {
//                            try {
//                                fos.close();
//                            } catch (Throwable var64) {
//                                var76.addSuppressed(var64);
//                            }
//                        } else {
//                            fos.close();
//                        }
//                    }
//
//                }
//
//            } catch (ParserConfigurationException var73) {
//                throw new IOException("Cannot create XML document builder: " + var73, var73);
//            } catch (XMLException var74) {
//                throw new IOException("Could not format XML document:" + var74, var74);
//            }
//        }
//    }
//
//    private Element writeAttributeMetaData(AttributeRole attributeRole, int sourcecol, Document document, boolean sparse) {
//        String tag = "attribute";
//        if (attributeRole.isSpecial()) {
//            tag = attributeRole.getSpecialName();
//        }
//
//        Attribute attribute = attributeRole.getAttribute();
//        return this.writeAttributeMetaData(tag, attribute, sourcecol, document, sparse);
//    }
//
//    private Element writeAttributeMetaData(String tag, Attribute attribute, int sourcecol, Document document, boolean sparse) {
//        Element attributeElement = document.createElement(tag);
//        attributeElement.setAttribute("name", attribute.getName());
//        if (!sparse || tag.equals("attribute")) {
//            attributeElement.setAttribute("sourcecol", sourcecol + "");
//        }
//
//        attributeElement.setAttribute("valuetype", Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(attribute.getValueType()));
//        if (!Ontology.ATTRIBUTE_BLOCK_TYPE.isA(attribute.getBlockType(), 1)) {
//            attributeElement.setAttribute("blocktype", Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(attribute.getBlockType()));
//        }
//
//        if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), 1) && !tag.equals(Attributes.KNOWN_ATTRIBUTE_TYPES[2])) {
//            Iterator var7 = attribute.getMapping().getValues().iterator();
//
//            while(var7.hasNext()) {
//                String nominalValue = (String)var7.next();
//                Element valueElement = document.createElement("value");
//                valueElement.setTextContent(nominalValue);
//                attributeElement.appendChild(valueElement);
//            }
//        }
//
//        return attributeElement;
//    }

    public String getExtension() {
        return "aml";
    }

    public String getFileDescription() {
        return "attribute description file";
    }

    public boolean equals(Object o) {
        if (!(o instanceof IExampleSet)) {
            return false;
        } else {
            IExampleSet es = (IExampleSet)o;
            return this.getAttributes().equals(es.getAttributes());
        }
    }

    public int hashCode() {
        return this.getAttributes().hashCode();
    }

//    public IOObject copy() {
//        return this.clone();
//    }

    public IExampleSet clone() {
        try {
            Class<? extends AbstractExampleSet> clazz = this.getClass();
            Constructor<? extends AbstractExampleSet> cloneConstructor = clazz.getConstructor(clazz);
            AbstractExampleSet result = (AbstractExampleSet)cloneConstructor.newInstance(this);
            result.idMap = this.idMap;
            return result;
        } catch (IllegalAccessException var4) {
            throw new RuntimeException("Cannot clone ExampleSet: " + var4.getMessage());
        } catch (NoSuchMethodException var5) {
            throw new RuntimeException("'" + this.getClass().getName() + "' does not implement clone constructor!");
        } catch (InvocationTargetException var6) {
            throw new RuntimeException("Cannot clone " + this.getClass().getName() + ": " + var6 + ". Target: " + var6.getTargetException() + ". Cause: " + var6.getCause() + ".");
        } catch (InstantiationException var7) {
            throw new RuntimeException("Cannot clone " + this.getClass().getName() + ": " + var7);
        }
    }

    public void remapIds() {
        this.idMap = new HashMap(this.size());
        IAttribute idAttribute = this.getAttributes().getSpecial("id");
        if (idAttribute != null) {
            int index = 0;

            for(Iterator var3 = this.iterator(); var3.hasNext(); ++index) {
                Example example = (Example)var3.next();
                double value = example.getValue(idAttribute);
                if (!Double.isNaN(value)) {
                    if (!this.idMap.containsKey(value)) {
                        this.idMap.put(value, new int[]{index});
                    } else {
                        int[] indices = (int[])this.idMap.get(value);
                        int[] newIndices = new int[indices.length + 1];

                        for(int i = 0; i < indices.length; ++i) {
                            newIndices[i] = indices[i];
                        }

                        newIndices[newIndices.length - 1] = index;
                        this.idMap.put(value, newIndices);
                    }
                }
            }
        }

    }

    public void recalculateAllAttributeStatistics() {
        List<IAttribute> allAttributes = new ArrayList();
        Iterator<IAttribute> a = this.getAttributes().allAttributes();

        while(a.hasNext()) {
            allAttributes.add(a.next());
        }

        this.recalculateAttributeStatistics((List)allAttributes);
    }

    public void recalculateAttributeStatistics(IAttribute attribute) {
        List<IAttribute> allAttributes = new ArrayList();
        allAttributes.add(attribute);
        this.recalculateAttributeStatistics((List)allAttributes);
    }

    private synchronized void recalculateAttributeStatistics(List<IAttribute> attributeList) {
        if (attributeList.size() != 0) {
            this.resetAttributeStatistics(attributeList);
            IAttribute weightAttribute = this.getAttributes().getWeight();
            if (weightAttribute != null && !weightAttribute.isNumerical()) {
                weightAttribute = null;
            }

            Iterator var3 = attributeList.iterator();

            do {
                IAttribute attribute;
                if (!var3.hasNext()) {
                    var3 = attributeList.iterator();

                    do {
                        if (!var3.hasNext()) {
                            return;
                        }

                        attribute = (IAttribute)var3.next();
                        List<IStatistics> tmpStatisticsList = new LinkedList();
                        Iterator<IStatistics> stats = attribute.getAllStatistics();

                        while(stats.hasNext()) {
                            IStatistics statistics = (IStatistics)((IStatistics)stats.next()).clone();
                            tmpStatisticsList.add(statistics);
                        }

                        this.statisticsMap.put(attribute.getName(), tmpStatisticsList);
                    } while(!Thread.currentThread().isInterrupted());

                    return;
                }

                attribute = (IAttribute)var3.next();
                Iterator var5;
                Example example;
                double value;
                if (weightAttribute == null) {
                    var5 = this.iterator();

                    while(var5.hasNext()) {
                        example = (Example)var5.next();
                        value = example.getValue(attribute);
                        double finalValue1 = value;
                        attribute.getAllStatistics().forEachRemaining((s) -> {
                            s.count(finalValue1, 1.0);
                        });
                    }
                } else {
                    var5 = this.iterator();

                    while(var5.hasNext()) {
                        example = (Example)var5.next();
                        value = example.getValue(attribute);
                        double weight = example.getValue(weightAttribute);
                        double finalValue = value;
                        attribute.getAllStatistics().forEachRemaining((s) -> {
                            s.count(finalValue, weight);
                        });
                    }
                }
            } while(!Thread.currentThread().isInterrupted());

            this.resetAttributeStatistics(attributeList);
        }
    }

    private void resetAttributeStatistics(List<IAttribute> attributeList) {
        Iterator var2 = attributeList.iterator();

        while(var2.hasNext()) {
            IAttribute attribute = (IAttribute)var2.next();
            Iterator<IStatistics> stats = attribute.getAllStatistics();

            while(stats.hasNext()) {
                IStatistics statistics = (IStatistics)stats.next();
                statistics.startCounting(attribute);
            }
        }

    }

    public double getStatistics(IAttribute attribute, String statisticsName) {
        return this.getStatistics(attribute, statisticsName, (String)null);
    }

    public double getStatistics(IAttribute attribute, String statisticsName, String statisticsParameter) {
        List<IStatistics> statisticsList = (List)this.statisticsMap.get(attribute.getName());
        if (statisticsList == null) {
            return Double.NaN;
        } else {
            Iterator var5 = statisticsList.iterator();

            IStatistics statistics;
            do {
                if (!var5.hasNext()) {
                    return Double.NaN;
                }

                statistics = (IStatistics)var5.next();
            } while(!statistics.handleStatistics(statisticsName));

            return statistics.getStatistics(attribute, statisticsName, statisticsParameter);
        }
    }

    public boolean isThreadSafeView() {
        return false;
    }

    public Annotations getAnnotations() {
        return this.annotations;
    }
}
