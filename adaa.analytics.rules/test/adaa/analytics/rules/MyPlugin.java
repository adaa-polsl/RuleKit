/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
//package adaa.analytics.rules;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Locale;
//import java.util.ResourceBundle;
//import java.util.jar.Attributes;
//import java.util.jar.JarOutputStream;
//import java.util.jar.Manifest;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.ProjectHelper;
//
//import com.rapidminer.gui.MainFrame;
//import com.rapidminer.gui.flow.ProcessRenderer;
//import com.rapidminer.gui.renderer.RendererService;
//import com.rapidminer.io.process.XMLImporter;
//import com.rapidminer.tools.I18N;
//import com.rapidminer.tools.OperatorService;
//import com.rapidminer.tools.plugin.Plugin;
//
///**
// * Klasa umozliwiajaca zaladowanie wtyczki RapidMiner na podstawie pliku Ant do
// * budowy wtyczki.
// *
// * @author Lukasz Wrobel
// */
//class MyPlugin {
//
//    private static final Logger LOG = Logger.getLogger(MyPlugin.class.getName());
//
//    private static final String DUMMY_PLUGIN_NAME = "dummyPlugin.jar";
//
//    private static final String TEMP_DIRECTORY_PREFIX = "disesor";
//
//    private String pluginName;
//
//    private Plugin dummyPlugin;
//
//    // Initialization-Class: com.rapidminer.PluginInit
//    // <property name="extension.initClass" value="com.rapidminer.PluginInit" />
//    private Class<?> pluginClass;
//
//    private ClassLoader classLoader;
//
//    // IOObject-Descriptor: /com/rapidminer/resources/ioobjects.xml
//    // <property name="extension.objectDefinition" value="/com/rapidminer/resources/ioobjects.xml" />
//    private String ioObjectDescriptor;
//
//    // Operator-Descriptor: /com/rapidminer/resources/Operators.xml
//    // <property name="extension.operatorDefinition" value="/com/rapidminer/resources/Operators.xml" />
//    private String operatorDescriptor;
//
//    // ParseRule-Descriptor: /com/rapidminer/resources/parserules.xml
//    // <property name="extension.parseRuleDefinition" value="/com/rapidminer/resources/parserules.xml" />
//    private String parseRulesDescriptor;
//
//    // Group-Descriptor: /com/rapidminer/resources/groups.properties
//    // <property name="extension.groupProperties" value="/com/rapidminer/resources/groups.properties" />
//    private String groupDescriptor;
//
//    // Error-Descriptor: /com/rapidminer/resources/i18n/Errors.properties
//    // <property name="extension.errorDescription" value="/com/rapidminer/resources/i18n/Errors.properties" />
//    private String errorDescriptor;
//
//    // UserError-Descriptor: /com/rapidminer/resources/i18n/UserErrorMessages.properties
//    // <property name="extension.userErrors" value="/com/rapidminer/resources/i18n/UserErrorMessages.properties" />
//    private String userErrorDescriptor;
//
//    // GUI-Descriptor: /com/rapidminer/resources/i18n/GUI.properties
//    // <property name="extension.guiDescription" value="/com/rapidminer/resources/i18n/GUI.properties" />
//    private String guiDescriptor;
//
//    /**
//     * Konstruktor.
//     * @param pluginBuildFile Plik Ant do budowy wtyczki
//     * @throws ClassNotFoundException
//     * @throws IOException
//     * @throws FileNotFoundException
//     */
//    MyPlugin(File pluginBuildFile) throws ClassNotFoundException, FileNotFoundException, IOException {
//
//        Project p = new Project();
//        p.init();
//        ProjectHelper.configureProject(p, pluginBuildFile);
//
//        pluginName = p.getProperty("extension.name");
//
//        pluginClass = Class.forName(p.getProperty("extension.initClass"));
//        classLoader = pluginClass.getClassLoader();
//
//        errorDescriptor = getProperty(p, "extension.errorDescription", true);
//        guiDescriptor = getProperty(p, "extension.guiDescription", true);
//        userErrorDescriptor = getProperty(p, "extension.userErrors", true);
//
//        ioObjectDescriptor = getProperty(p, "extension.objectDefinition");
//        operatorDescriptor = getProperty(p, "extension.operatorDefinition");
//        parseRulesDescriptor = getProperty(p, "extension.parseRuleDefinition");
//
//        groupDescriptor = getProperty(p, "extension.groupProperties");
//
//        dummyPlugin = createDummyPlugin(p);
//    }
//
//    void initPlugin() {
//        ReflectionUtils.callMethod(pluginClass, "initPlugin");
//    }
//
//    void initGui(MainFrame mainframe) {
//        ReflectionUtils.callMethod(pluginClass, "initGui", new Class[] { MainFrame.class }, new Object[] { mainframe });
//    }
//
//    void initFinalChecks() {
//        ReflectionUtils.callMethod(pluginClass, "initFinalChecks");
//    }
//
//    void registerDescriptors() {
//
//        // pliki jezykowe
//        I18N.registerErrorBundle(getResourceBundle(errorDescriptor));
//        I18N.registerGUIBundle(getResourceBundle(guiDescriptor));
//        I18N.registerUserErrorMessagesBundle(getResourceBundle(userErrorDescriptor));
//
//        // rejestracja renderow dla obiektow typy IOObject
//        RendererService.init(pluginName, getResource(ioObjectDescriptor), classLoader);
//
//        // rejestracja regul parsowania
//        XMLImporter.importParseRules(getResource(parseRulesDescriptor), dummyPlugin);
//
//        // rejestracja kolorow i grup
//        if (groupDescriptor != null) {
//            ProcessRenderer.registerAdditionalObjectColors(groupDescriptor, pluginName, classLoader);
//            ProcessRenderer.registerAdditionalGroupColors(groupDescriptor, pluginName, classLoader);
//        } else {
//            LOG.log(Level.WARNING, "Missing descriptor {0}", groupDescriptor);
//        }
//    }
//
//    /**
//     * Rejestracja operatorow.
//     */
//    void registerOperators() {
//        InputStream in = null;
//
//        URL operatorsUrl = getResource(operatorDescriptor);
//        if (operatorsUrl != null) {
//            try {
//                in = operatorsUrl.openStream();
//                OperatorService.registerOperators(pluginName, in, classLoader, dummyPlugin);
//            } catch (IOException e) {
//                LOG.log(Level.WARNING, e.getMessage(), e);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        LOG.log(Level.WARNING, e.getMessage(), e);
//                    }
//                }
//            }
//        }
//    }
//
//    private String getProperty(Project p, String key) {
//        return getProperty(p, key, false);
//    }
//
//    private String getProperty(Project p, String key, boolean convertToDotNotation) {
//        String descriptor = p.getProperty(key);
//        if (descriptor == null) {
//            return null;
//        }
//
//        if (convertToDotNotation) {
//            // usuniecie rozszerzenia z nazwy
//            int pos = descriptor.lastIndexOf(".");
//            if (pos > 0) {
//                descriptor = descriptor.substring(0, pos);
//            }
//        }
//
//        if (descriptor.startsWith("/")) {
//            descriptor = descriptor.substring(1);
//        }
//
//        if (convertToDotNotation) {
//            descriptor = descriptor.replace('/', '.');
//        }
//
//        return descriptor;
//    }
//
//    private URL getResource(String name) {
//        URL resource = null;
//        if (name != null) {
//            resource = classLoader.getResource(name);
//            if (resource == null) {
//                LOG.log(Level.WARNING, "Unable to load {0}", name);
//            }
//        } else {
//            LOG.log(Level.WARNING, "Missing descriptor {0}", name);
//        }
//
//        return resource;
//    }
//
//    private ResourceBundle getResourceBundle(String baseName) {
//        ResourceBundle bundle = null;
//        if (baseName != null) {
//            bundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), classLoader);
//            if (bundle == null) {
//                LOG.log(Level.WARNING, "Unable to load {0}", baseName);
//            }
//        } else {
//            LOG.log(Level.WARNING, "Missing descriptor {0}", baseName);
//        }
//
//        return bundle;
//    }
//
//    /**
//     * Aby umozliwic odczytanie m.in. namespace dla wtyczki tworzymy plik jar wtyczki
//     * zawierajacy tylko plik manifestu - klasa Plugin posiada jedynie konstruktor Plugin(File file).
//     * Dzieki temu mozemy stworzyc pusty plugin, ktory nastepnie przekazujemy jako parametr 'provider'
//     * m.in. do funkcji rejestrujacej operatory.
//     */
//    private Plugin createDummyPlugin(Project p) {
//
//        Manifest manifest = new Manifest();
//        Attributes attributes = manifest.getMainAttributes();
//        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
//
//        attributes.put(new Attributes.Name("Implementation-Vendor"), p.getProperty("extension.vendor"));
//        attributes.put(new Attributes.Name("Implementation-Title"), p.getProperty("extension.name"));
//        attributes.put(new Attributes.Name("Implementation-URL"), p.getProperty("extension.url"));
//        attributes.put(new Attributes.Name("Implementation-Version"), p.getProperty("extension.longversion"));
//        attributes.put(new Attributes.Name("Specification-Title"), p.getProperty("extension.name"));
//        attributes.put(new Attributes.Name("Specification-Version"), p.getProperty("extension.longversion"));
//        attributes.put(new Attributes.Name("RapidMiner-Version"), p.getProperty("extension.needsVersion"));
//        attributes.put(new Attributes.Name("RapidMiner-Type"), p.getProperty("RapidMiner_Extension"));
//        attributes.put(new Attributes.Name("Plugin-Dependencies"), p.getProperty("extension.dependencies"));
//        attributes.put(new Attributes.Name("Extension-ID"), p.getProperty("extension.updateServerId"));
//        attributes.put(new Attributes.Name("Namespace"), p.getProperty("extension.namespace"));
//        attributes.put(new Attributes.Name("Initialization-Class"), p.getProperty("extension.initClass"));
//        attributes.put(new Attributes.Name("IOObject-Descriptor"), p.getProperty("extension.objectDefinition"));
//        attributes.put(new Attributes.Name("Operator-Descriptor"), p.getProperty("extension.operatorDefinition"));
//        attributes.put(new Attributes.Name("ParseRule-Descriptor"), p.getProperty("extension.parseRuleDefinition"));
//        attributes.put(new Attributes.Name("Group-Descriptor"), p.getProperty("extension.groupProperties"));
//        attributes.put(new Attributes.Name("Error-Descriptor"), p.getProperty("extension.errorDescription"));
//        attributes.put(new Attributes.Name("UserError-Descriptor"), p.getProperty("extension.userErrors"));
//        attributes.put(new Attributes.Name("GUI-Descriptor"), p.getProperty("extension.guiDescription"));
//
//        Plugin plugin = null;
//        Path tempFolder = null;
//        try {
//            tempFolder = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX);
//            File tempFile = Paths.get(tempFolder.toString(), DUMMY_PLUGIN_NAME).toFile();
//
//            JarOutputStream jar = new JarOutputStream(new FileOutputStream(tempFile), manifest);
//            jar.close();
//
//            plugin = new Plugin(tempFile);
//
//        } catch (IOException e) {
//            LOG.warning("Cannot create jar file of dummy plugin");
//        }
//        finally {
//            if (plugin != null) {
//                try {
//                    plugin.getArchive().close();
//                } catch (IOException e) {
//                    LOG.warning("Cannot close jar file of dummy plugin");
//                }
//            }
//
//            deleteTempFolder(tempFolder);
//        }
//
//        return plugin;
//    }
//
//    private void deleteTempFolder(Path tempFolder) {
//        if (tempFolder != null) {
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempFolder)) {
//                for (Path entry : stream) {
//                    if (Files.isDirectory(entry)) {
//                        deleteTempFolder(entry);
//                    } else {
//                        try {
//                            Files.delete(entry);
//                        } catch (SecurityException | IOException e) {
//                            LOG.warning("Failed to delete temp file " + entry.toAbsolutePath());
//                        }
//                    }
//                }
//            } catch (IOException e1) {
//                LOG.warning("Failed to delete temp files");
//            }
//
//            try {
//                Files.delete(tempFolder);
//            } catch (SecurityException | IOException e) {
//                LOG.warning("Failed to delete temp folder " + tempFolder.toAbsolutePath());
//            }
//        }
//    }
//}
