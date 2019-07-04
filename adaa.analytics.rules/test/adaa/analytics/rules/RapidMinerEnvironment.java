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
//import java.awt.Dimension;
//import java.awt.Toolkit;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.Arrays;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
//import com.rapid_i.Launcher;
//import com.rapidminer.RapidMiner;
//import com.rapidminer.RapidMiner.ExecutionMode;
//import com.rapidminer.gui.GUIInputHandler;
//import com.rapidminer.gui.MainFrame;
//import com.rapidminer.gui.RapidMinerGUI;
//import com.rapidminer.gui.docking.RapidDockableContainerFactory;
//import com.rapidminer.gui.look.RapidLookAndFeel;
//import com.rapidminer.gui.look.fc.BookmarkIO;
//import com.rapidminer.gui.look.ui.RapidDockingUISettings;
//import com.rapidminer.gui.renderer.RendererService;
//import com.rapidminer.gui.tools.SplashScreen;
//import com.rapidminer.gui.tools.SwingTools;
//import com.rapidminer.gui.tools.VersionNumber;
//import com.rapidminer.io.process.XMLImporter;
//import com.rapidminer.repository.RepositoryManager;
//import com.rapidminer.tools.FileSystemService;
//import com.rapidminer.tools.GlobalAuthenticator;
//import com.rapidminer.tools.I18N;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.tools.OperatorService;
//import com.rapidminer.tools.ParameterService;
//import com.rapidminer.tools.XMLSerialization;
//import com.rapidminer.tools.cipher.CipherTools;
//import com.rapidminer.tools.cipher.KeyGenerationException;
//import com.rapidminer.tools.cipher.KeyGeneratorTool;
//import com.rapidminer.tools.config.ConfigurationManager;
//import com.rapidminer.tools.jdbc.DatabaseService;
//import com.rapidminer.tools.jdbc.connection.DatabaseConnectionService;
//import com.rapidminer.tools.plugin.Plugin;
//import com.vlsolutions.swing.docking.DockableContainerFactory;
//import com.vlsolutions.swing.docking.ui.DockingUISettings;
//
///**
// * Klasa umozliwiajaca uruchomienie wtyczki w roznych konfiguracjach srodowiska RapidMiner.
// * <p>
// * Klasa pozwala m.in. na:
// * <ul>
// * <li>wylaczenie ekranu startowego RapidMiner,</li>
// * <li>pominiecie ladowania operatorow wbudowanych w RapidMiner'a,</li>
// * <li>pominiecie ladowania sterownikow do bazy danych,</li>
// * <li>pominiecie ladowania wtyczek z plikow .jar.</li>
// * </ul>
// *  </p>
// *  Przeznaczeniem klasy jest debugowanie i testowania wtyczki w programie RapidMiner
// *  bezposrednio z poziomu srodowiska programistycznego takiego jak Eclipse
// *  (zamiast budowania .jar wtyczki za pomoca programu Ant i osobnego uruchamiania
// *  srodowiska RapidMiner). Mozliwosc wylaczenia wybranych elementow programu RapidMiner
// *  czesto znaczenie przyspiesza czas ladowania calego srodowiska.
// *
// * @author Lukasz Wrobel
// */
//public final class RapidMinerEnvironment {
//
//    private static final Logger LOG = Logger.getLogger(RapidMinerEnvironment.class.getName());
//
//    private static final String PLUGIN_BUILD_FILE_NAME = "build.xml";
//
//    private static final String OPERATORS_MINIMAL_PATH =
//            "/disesor/rapidminer/development/resources/OperatorsMinimal.xml";
//
//    /**
//     * Prefiksy pol, ktore steruja jakie elementy srodowiska RapidMiner sa uruchamiane.
//     */
//    private static final List<String> SETTINGS_FIELDS_PREFIXES = Arrays.asList("init", "load", "show");
//
//    //
//    // NOTE: przy dodawaniu nowego pola nalezy pamietac rowniez o modyfikacji klasy Builder
//    //
//    private boolean initCipher;
//
//    private boolean initConfigurationManager;
//
//    private boolean initCoreOperators;
//
//    private boolean initDatabaseService;
//
//    private boolean initGlobalAuthenticator;
//
//    private boolean initLookAndFeel;
//
//    private boolean initRendererService;
//
//    private boolean initRepositoryManager;
//
//    private boolean initXmlImporter;
//
//    private boolean initXmlSerialization;
//
//    private boolean loadExternalPlugins;
//
//    private boolean loadIcons;
//
//    private boolean loadUserPerspectives;
//
//    private boolean showSplash;
//
//    private RapidMinerEnvironment(Builder builder) {
//        List<Field> thisFields = ReflectionUtils.getFieldsStartingWith(this, SETTINGS_FIELDS_PREFIXES);
//        for (Field field : thisFields) {
//            Object value = ReflectionUtils.getFieldValue(builder, field.getName());
//            ReflectionUtils.setFieldValue(this, field, value);
//        }
//    }
//
//    /**
//     * Uruchamia srodowisko RapidMiner.
//     * @throws Exception
//     */
//    public void run() throws Exception {
//        runPlugin(null);
//    }
//
//    /**
//     * Uruchamia plugin na podstawie pliku build.xml znajdujacego sie w glownym katalogu projektu.
//     * @throws Exception
//     */
//    public void runPlugin() throws Exception {
//        URL url = RapidMinerEnvironment.class.getClassLoader().getResource(".");
//        File dir = new File(new URI(url.toString()));
//
//        File pluginBuildFile = new File(dir.getParentFile(), PLUGIN_BUILD_FILE_NAME);
//        runPlugin(pluginBuildFile);
//    }
//
//    /**
//     * Uruchamia plugin na postawie podanego pliku Ant'a.
//     * @param pluginBuildFile
//     * @throws Exception
//     */
//    public void runPlugin(File pluginBuildFile) throws Exception {
//
//        MyPlugin plugin = null;
//        if (pluginBuildFile != null) {
//            plugin = new MyPlugin(pluginBuildFile);
//        }
//
//        System.setSecurityManager(null);
//        RapidMiner.setExecutionMode(ExecutionMode.UI);
//
//        RapidMiner.addShutdownHook(new Runnable() {
//            @Override
//            public void run() {
//                RepositoryManager.shutdown();
//            }
//        });
//
//        RapidMiner.setInputHandler(new GUIInputHandler());
//
//        // inicjalizacja Docking UI - musi byc robiona jak najwczesniej
//        DockingUISettings.setInstance(new RapidDockingUISettings());
//        DockableContainerFactory.setFactory(new RapidDockableContainerFactory());
//
//        if (showSplash) {
//            RapidMiner.showSplash();
//        }
//
//        // inicjalizacja serwisow RapidMiner
//        splashMessage("basic");
//        initServices(plugin);
//
//        // inicjalizacja GUI
//        splashMessage("workspace");
//        splashMessage("plaf");
//
//        String userRapidMinerDir = FileSystemService.getUserRapidMinerDir().getAbsolutePath();
//        System.setProperty(BookmarkIO.PROPERTY_BOOKMARKS_DIR, userRapidMinerDir);
//        System.setProperty(BookmarkIO.PROPERTY_BOOKMARKS_FILE, ".bookmarks");
//        System.setProperty(DatabaseConnectionService.PROPERTY_CONNECTIONS_FILE, "connections");
//
//        if (initLookAndFeel) {
//            try {
//                UIManager.setLookAndFeel(new RapidLookAndFeel());
//            } catch (Exception e) {
//                String msg = I18N.getMessage(
//                    LogService.getRoot().getResourceBundle(),
//                    "com.rapidminer.gui.RapidMinerGUI.setting_up_modern_look_and_feel_error");
//
//                LOG.log(Level.WARNING, msg, e);
//            }
//        }
//
//        if (loadIcons) {
//            splashMessage("icons");
//            SwingTools.loadIcons();
//        }
//
//        if (initRepositoryManager) {
//            RepositoryManager.getInstance(null).createRepositoryIfNoneIsDefined();
//        }
//
//        // inicjalizacja glowego okna programu
//        splashMessage("create_frame");
//
//        SwingUtilities.invokeAndWait(new Runnable() {
//            @Override
//            public void run() {
//                RapidMinerGUI.setMainFrame(new MainFrame("welcome"));
//            }
//        });
//
//        splashMessage("gui_properties");
//        MainFrame mainFrame = RapidMinerGUI.getMainFrame();
//
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        mainFrame.setLocation((int) (0.05d * screenSize.getWidth()), (int) (0.05d * screenSize.getHeight()));
//        mainFrame.setSize((int) (0.9d * screenSize.getWidth()), (int) (0.9d * screenSize.getHeight()));
//
//        mainFrame.setExpertMode(true);
//
//        if (loadUserPerspectives) {
//            mainFrame.getPerspectives().loadAll();
//        }
//
//        // wywolanie metody initGui wtyczek
//        if (loadExternalPlugins) {
//            splashMessage("plugin_gui");
//            Plugin.initPluginGuis(mainFrame);
//        }
//
//        if (plugin != null) {
//            plugin.initGui(mainFrame);
//        }
//
//        splashMessage("show_frame");
//        mainFrame.setVisible(true);
//
//        // wywolanie metody initFinalChecks wtyczek
//        if (loadExternalPlugins) {
//            splashMessage("checks");
//            Plugin.initFinalChecks();
//        }
//
//        if (plugin != null) {
//            plugin.initFinalChecks();
//        }
//
//        splashMessage("ready");
//
//        if (showSplash) {
//            RapidMiner.hideSplash();
//        }
//    }
//
//    private void initServices(MyPlugin plugin) {
//
//        splashMessage("init_i18n");
//        I18N.getErrorBundle();
//
//        // upewnienie sie, ze katalog programu jest ustawiony
//        splashMessage("rm_home");
//        setRapidMinerHome();
//        //ParameterService.ensureRapidMinerHomeSet();
//
//        // inicjalizacja ustawien
//        splashMessage("init_parameter_service");
//        performInitialSettings();
//        ParameterService.init();
//
//        // initializing networking tools
//        if (initGlobalAuthenticator) {
//            GlobalAuthenticator.init();
//        }
//
//        // inicjalizacja repozytorium
//        if (initRepositoryManager) {
//            splashMessage("init_repository");
//            RepositoryManager.init();
//        }
//
//        // rejestracja pluginow
//        if (loadExternalPlugins) {
//            splashMessage("register_plugins");
//            setPluginLocation();
//
//            if (!showSplash) {
//                // podczas ladowania wtyczki wywolywana jest:
//                // RapidMiner.getSplashScreen().addExtension(this);
//                // co powoduje NullPointerException gdy showSplash == false
//                // inicjalizujemy tymczasowy splashscreen (ale go nie pokazujemy)
//                try {
//                    Field field = RapidMiner.class.getDeclaredField("splashScreen");
//                    field.setAccessible(true);
//                    Field modifiersField = Field.class.getDeclaredField("modifiers");
//                    modifiersField.setAccessible(true);
//                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//                    field.set(null, new SplashScreen(RapidMiner.getShortVersion(), null));
//                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//
//            Plugin.initAll();
//
//            if (!showSplash) {
//                // dispose tymczasowego splashscreen
//                RapidMiner.hideSplash();
//            }
//        }
//
//        if (plugin != null) {
//            plugin.registerDescriptors();
//            plugin.initPlugin();
//        }
//
//        // rejestracja operatorow
//        if (!initCoreOperators) {
//            // minimalny zestaw operatorow konieczny do dzialania srodowiska
//            // plik OperatorsMinimal.xml zawiera tylko operator glowego procesu
//            System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_INIT_OPERATORS, OPERATORS_MINIMAL_PATH);
//        }
//
//        splashMessage("init_ops");
//        OperatorService.init();
//
//        if (plugin != null) {
//            plugin.registerOperators();
//        }
//
//        if (initXmlImporter) {
//            splashMessage("xml_transformer");
//            XMLImporter.init();
//        }
//
//        // tworzenie klucza szyfrowania
//        if (initCipher && !CipherTools.isKeyAvailable()) {
//            splashMessage("gen_key");
//            try {
//                KeyGeneratorTool.createAndStoreKey();
//            } catch (KeyGenerationException e) {
//                String msg = I18N.getMessage(
//                        LogService.getRoot().getResourceBundle(),
//                        "com.rapidminer.RapidMiner.generating_encryption_key_error", e.getMessage());
//
//                LOG.log(Level.WARNING, msg, e);
//            }
//        }
//
//        if (initDatabaseService) {
//            splashMessage("load_jdbc_drivers");
//            DatabaseService.init();
//            DatabaseConnectionService.init();
//        }
//
//        if (initConfigurationManager) {
//            splashMessage("init_configurables");
//            ConfigurationManager.getInstance().initialize();
//        }
//
//        // inicjalizacja renderow dla IOObject
//        if (initRendererService) {
//            splashMessage("init_renderers");
//            RendererService.init();
//        }
//
//        // inicjalizacja serializacji xml
//        if (initXmlSerialization) {
//            splashMessage("xml_serialization");
//            XMLSerialization.init(Plugin.getMajorClassLoader());
//        }
//    }
//
//    private void performInitialSettings() {
//
//        boolean firstStart = false;
//        boolean versionChanged = false;
//        VersionNumber lastVersionNumber = null;
//        VersionNumber currentVersionNumber = new VersionNumber(RapidMiner.getLongVersion());
//
//        File lastVersionFile = new File(FileSystemService.getUserRapidMinerDir(), "lastversion");
//        if (!lastVersionFile.exists()) {
//            firstStart = true;
//        } else {
//            String versionString = null;
//            BufferedReader in = null;
//            try {
//                in = new BufferedReader(new FileReader(lastVersionFile));
//                versionString = in.readLine();
//            } catch (IOException e) {
//                String msg = I18N.getMessage(
//                    LogService.getRoot().getResourceBundle(),
//                    "com.rapidminer.RapidMiner.reading_global_version_file_error");
//                LOG.log(Level.WARNING, msg, e);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        LogService.getRoot().log(Level.WARNING,
//                                I18N.getMessage(LogService.getRoot().getResourceBundle(),
//                                		"com.rapidminer.RapidMiner.closing_stream_error", lastVersionFile), e);
//                    }
//                }
//            }
//
//            if (versionString != null) {
//                lastVersionNumber = new VersionNumber(versionString);
//                if (currentVersionNumber.compareTo(lastVersionNumber) > 0) {
//                    firstStart = true;
//                }
//                if (currentVersionNumber.compareTo(lastVersionNumber) != 0) {
//                    versionChanged = true;
//                }
//            } else {
//                firstStart = true;
//            }
//        }
//
//        // init this version (workspace etc.)
//        if (firstStart) {
//            if (currentVersionNumber != null) {
//                LogService.getRoot().log(
//                        Level.INFO, "com.rapidminer.RapidMiner.performing_upgrade",
//                        new Object[] {
//                            (lastVersionNumber != null ? " from version " + lastVersionNumber : ""),
//                            currentVersionNumber
//                        });
//            }
//
//            // copy old settings to new version file
//            ParameterService.copyMainUserConfigFile(lastVersionNumber, currentVersionNumber);
//        }
//
//        if (firstStart || versionChanged) {
//            // write version file
//            PrintWriter out = null;
//            try {
//                out = new PrintWriter(new FileWriter(lastVersionFile));
//                out.println(RapidMiner.getLongVersion());
//            } catch (IOException e) {
//                String msg = I18N.getMessage(
//                        LogService.getRoot().getResourceBundle(),
//                        "com.rapidminer.RapidMiner.writing_current_version_error");
//                LOG.log(Level.WARNING, msg, e);
//            } finally {
//                if (out != null) {
//                    out.close();
//                }
//            }
//        }
//    }
//
//    private void splashMessage(String messageKey) {
//        if (showSplash) {
//            RapidMiner.splashMessage(messageKey);
//        }
//    }
//
//    private void setRapidMinerHome() {
//        String message = "Trying base directory of classes (build) '";
//        URL url = Launcher.class.getClassLoader().getResource(".");
//        if (url != null) {
//            try {
//                File dir = new File(new URI(url.toString()));
//                if (dir.exists()) {
//                    dir = dir.getParentFile();
//                    message += dir + "'...";
//                    if (dir != null) {
//                        message += "gotcha!";
//                        try {
//                            System.setProperty(Launcher.PROPERTY_RAPIDMINER_HOME, dir.getCanonicalPath());
//                        } catch (IOException e) {
//                            System.setProperty(Launcher.PROPERTY_RAPIDMINER_HOME, dir.getAbsolutePath());
//                        }
//                    } else {
//                        message += "failed";
//                    }
//                } else {
//                    message += "failed";
//                }
//            } catch (Throwable e) {
//                // important: not only URI Syntax Exception since the program must not crash in any case!!!
//                // For example: RapidNet integration as applet into Siebel would cause problem with new File(...)
//                message += "failed";
//            }
//        } else {
//            message += "failed";
//        }
//
//        LOG.log(Level.INFO, message);
//    }
//
//    private void setPluginLocation() {
//        try {
//            URI path = RapidMiner.class.getResource(RapidMiner.class.getSimpleName() + ".class").toURI();
//            if ("file".equals(path.getScheme())) {
//                String parentDir = new File(path).getParent();
//                String packageDir = RapidMiner.class.getPackage().getName().replace('.', '\\');
//
//                if (parentDir.endsWith(packageDir)) {
//                    // glowny katalog z klasami RapidMiner
//                    parentDir = parentDir.substring(0, parentDir.length() - packageDir.length());
//                }
//
//                // katalog projektu rapidminer
//                String rapidMinerDir = new File(parentDir).getParent();
//
//                // katalog z wtyczkami
//                String pluginDir = rapidMinerDir + "\\lib\\plugins\\";
//                Plugin.setPluginLocation(pluginDir);
//                LOG.log(Level.INFO, "RapidMiner's plugin location set to: " + pluginDir);
//            } else {
//                LOG.log(Level.WARNING, "RapidMiner's \\lib\\plugins directory could not be found: "
//                        + "wrong URI of RapidMiner.class (" + path + ")");
//            }
//        } catch (URISyntaxException e) {
//            LOG.log(Level.WARNING, "RapidMiner's \\lib\\plugins directory not found: wrong URI.", e);
//        }
//    }
//
//    /**
//     * Budowniczy, umozliwia skonfigurowanie srodowiska RapidMiner.
//     *
//     * @author Lukasz Wrobel
//     */
//    public static class Builder {
//
//        // Pola wykorzysytwane sa tylko poprzez mechanizm refleksji,
//        // stad adnotacja @SuppressWarnings("unused")
//
//        @SuppressWarnings("unused")
//        private boolean initCipher;
//
//        @SuppressWarnings("unused")
//        private boolean initConfigurationManager;
//
//        @SuppressWarnings("unused")
//        private boolean initCoreOperators;
//
//        @SuppressWarnings("unused")
//        private boolean initDatabaseService;
//
//        @SuppressWarnings("unused")
//        private boolean initGlobalAuthenticator;
//
//        @SuppressWarnings("unused")
//        private boolean initLookAndFeel;
//
//        @SuppressWarnings("unused")
//        private boolean initRendererService;
//
//        @SuppressWarnings("unused")
//        private boolean initRepositoryManager;
//
//        @SuppressWarnings("unused")
//        private boolean initXmlImporter;
//
//        @SuppressWarnings("unused")
//        private boolean initXmlSerialization;
//
//        @SuppressWarnings("unused")
//        private boolean loadExternalPlugins;
//
//        @SuppressWarnings("unused")
//        private boolean loadIcons;
//
//        @SuppressWarnings("unused")
//        private boolean loadUserPerspectives;
//
//        @SuppressWarnings("unused")
//        private boolean showSplash;
//
//        /**
//         * Tworzy klase Builder'a z domyslnie wlaczonymi wszystkimi elementami srodowiska RapidMiner.
//         */
//        public Builder() {
//            this(true);
//        }
//
//        /**
//         * Tworzy klase Builder'a ze wszystkimi parametrami ustawionymi na podana wartosc.
//         */
//        public Builder(boolean allEnabledByDefault) {
//            List<Field> builderFields = ReflectionUtils.getFieldsStartingWith(this, SETTINGS_FIELDS_PREFIXES);
//            for (Field field : builderFields) {
//                ReflectionUtils.setFieldValue(this, field, allEnabledByDefault);
//            }
//        }
//
//        public Builder initCipher(boolean initCipher) {
//            this.initCipher = initCipher;
//            return this;
//        }
//
//        public Builder initConfigurationManager(boolean initConfigurationManager) {
//            this.initConfigurationManager = initConfigurationManager;
//            return this;
//        }
//
//        /**
//         * Inicjalizacja standardowych operatorow RapidMiner.
//         */
//        public Builder initCoreOperators(boolean initCoreOperators) {
//            this.initCoreOperators = initCoreOperators;
//            return this;
//        }
//
//        /**
//         * Inicjalizacja serwisu do obslugi bazy danych.
//         */
//        public Builder initDatabaseService(boolean initDatabaseService) {
//            this.initDatabaseService = initDatabaseService;
//            return this;
//        }
//
//        public Builder initGlobalAuthenticator(boolean initGlobalAuthenticator) {
//            this.initGlobalAuthenticator = initGlobalAuthenticator;
//            return this;
//        }
//
//        /**
//         * Inicjalizacja stylu wygladu okien.
//         */
//        public Builder initLookAndFeel(boolean initLookAndFeel) {
//            this.initLookAndFeel = initLookAndFeel;
//            return this;
//        }
//
//        /**
//         * Inicjalizacja obslugi renderowania obiektow typu IOObject.
//         */
//        public Builder initRendererService(boolean initRendererService) {
//            this.initRendererService = initRendererService;
//            return this;
//        }
//
//        /**
//         * Inicjalizacja repozytorium.
//         */
//        public Builder initRepositoryManager(boolean initRepositoryManager) {
//            this.initRepositoryManager = initRepositoryManager;
//            return this;
//        }
//
//        public Builder initXmlImporter(boolean initXmlImporter) {
//            this.initXmlImporter = initXmlImporter;
//            return this;
//        }
//
//        public Builder initXmlSerialization(boolean initXmlSerialization) {
//            this.initXmlSerialization = initXmlSerialization;
//            return this;
//        }
//
//        /**
//         * Wczytanie rozszerzen z plikow .jar
//         */
//        public Builder loadExternalPlugins(boolean loadExternalPlugins) {
//            this.loadExternalPlugins = loadExternalPlugins;
//            return this;
//        }
//
//        /**
//         * Wczytanie ikon.
//         */
//        public Builder loadIcons(boolean loadIcons) {
//            this.loadIcons = loadIcons;
//            return this;
//        }
//
//        /**
//         * Wczytanie wczesniej zdefiniowanych perspektywy uzytkownika.
//         */
//        public Builder loadUserPerspectives(boolean loadUserPerspectives) {
//            this.loadUserPerspectives = loadUserPerspectives;
//            return this;
//        }
//
//        /**
//         * Okresla czy ma byc pokazywany ekran startowy.
//         * @param showSplash Jezeli false to ekran startowy nie jest pokazywany.
//         * @return
//         */
//        public Builder showSplash(boolean showSplash) {
//            this.showSplash = showSplash;
//            return this;
//        }
//
//        /**
//         * Tworzy srodowisko.
//         * @return
//         */
//        public RapidMinerEnvironment build() {
//            return new RapidMinerEnvironment(this);
//        }
//    }
//}
