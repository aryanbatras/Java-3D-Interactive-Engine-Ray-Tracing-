import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.List;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Window extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    BufferedImage IMG, BEINGRENDERED, SAVEDIMAGE;
    int HEIGHT, WIDTH, ANTI_ALISING_SAMPLES;
    double V_YAXIS, V_XAXIS, V_ZAXIS, M_X, M_Y, M_Z, latestX, latestY;
    double yaw, pitch, radius; boolean drag; int go;

    Color pixelColor; Random random;
    Dimension SCREEN; Camera CAMERA; Camera RASTERIZERCAMERA; ArrayList<Shape> WORLD;
    ScheduledExecutorService mouseClock, keyClock;
    ScheduledFuture<?> mouseSchedule, keySchedule;

    JLabel LabelX, LabelY, LabelZ;JTextField LabelChangerX, LabelChangerY, LabelChangerZ; Font LabelFont, UpdateFont;
    Image ArrowsImage, ZbuttonImage, XbuttonImage, MouseImage;

    JDialog olderEditObjectWindow; JPanel viewObjects; int objectCounter;
    JProgressBar saveImageProgress; JScrollPane viewObjectsScrolling;

    Shape selectedShape; JDialog axis; JCheckBox selectObjectMouseCheckbox, hideUICheckbox;
    JWindow specialFeaturesWindow, saveLoadObjectWindow, saveButtonWindow, mouseKeyWindow, objectWindow;
    BufferedImage environmentMap; Map<Shape, JButton> shapeButtonMap;
    String[] selectedloadScene;

    Window() throws IOException {
        setup();
        window();
        overlay();
    }

    void overlay() {
        Components();
        axisWindow();
        objectWindow();
        MouseKeyWindow();
        saveImageWindow();
        specialFeaturesWindow();
        closeApplicationWindow();
        saveLoadRandomObjectWindow();
        Window.this.requestFocus();
        refreshShapeButtons();
    }


    void closeApplicationWindow(){
        JWindow closeApplicationWindow = new JWindow();
        closeApplicationWindow.setBackground(new java.awt.Color(10,10,10, 10));
        closeApplicationWindow.setSize(getWidth() / 32, getHeight() / 38);
        closeApplicationWindow.setLocation(getWidth() / 250,getHeight() / 250);
        closeApplicationWindow.setAlwaysOnTop(true);
        closeApplicationWindow.setLayout(null);

        closeApplicationWindow.add(new JButton(" Esc "){
            {
                setSize(closeApplicationWindow.getWidth(), closeApplicationWindow.getHeight());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new java.awt.Color(10, 10, 10), 0),
                        BorderFactory.createEmptyBorder(8,8,8,8)
                ));
                setForeground(java.awt.Color.lightGray);
                setLocation(0,0);

                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addActionListener(e -> {
                    System.exit(0);
                });

            }
        });
        closeApplicationWindow.setVisible(true);
    }

    void specialFeaturesWindow(){

        specialFeaturesWindow = new JWindow();
        specialFeaturesWindow.setBackground(new java.awt.Color(0,0,0,0));
        specialFeaturesWindow.setSize(getWidth() / 8, getHeight() / 4);
        specialFeaturesWindow.setLocation(0, specialFeaturesWindow.getHeight() * 2 );
        specialFeaturesWindow.setAlwaysOnTop(true);
        specialFeaturesWindow.setLayout(null);

        specialFeaturesWindow.add(selectObjectMouseCheckbox(specialFeaturesWindow));
        specialFeaturesWindow.add(hideUICheckbox(specialFeaturesWindow));
        specialFeaturesWindow.add(loadSceneMenu(specialFeaturesWindow));

        specialFeaturesWindow.setVisible(true);
    }

    JComboBox<String> loadSceneMenu(JWindow specialFeaturesWindow) {

        String[] loadSceneText = {
                "PureSky", "Room", "MirrorHall", "ModernHall", "ChristmasHall", "Lounge", "Garden", "Backyard", "Lake", "Pool"
        };


        JComboBox<String> loadSceneMenu = new JComboBox<>(loadSceneText);
        loadSceneMenu.setSize(specialFeaturesWindow.getWidth() - specialFeaturesWindow.getWidth() / 25, specialFeaturesWindow.getHeight() / 4);
        loadSceneMenu.setLocation(specialFeaturesWindow.getWidth() / 25, specialFeaturesWindow.getHeight() * 3 / 4);
        loadSceneMenu.setSelectedItem(selectedloadScene[0]);
        loadSceneMenu.addActionListener(e -> {
            selectedloadScene[0] = (String) loadSceneMenu.getSelectedItem();
            try{
                switch (selectedloadScene[0]) {
                    case "PureSky": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/puresky.jpg")); break;
                    case "Room": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/room.jpg")); break;
                    case "MirrorHall": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/mirrorhall.jpg")); break;
                    case "ModernHall": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/modernhall.jpg")); break;
                    case "ChristmasHall": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/christmashall.jpg")); break;
                    case "Lounge": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/lounge.jpg")); break;
                    case "Garden": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/garden.jpg")); break;
                    case "Backyard": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/backyard.jpg")); break;
                    case "Lake": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/lake.jpg")); break;
                    case "Pool": environmentMap = ImageIO.read(getClass().getResourceAsStream("/Resources/Scenes/pool.jpg")); break;
                }
            }
            catch(IOException ex){
                throw new RuntimeException(ex);
            };

            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);

        });
        return  loadSceneMenu;
    }

    JButton closeApplicationButton(JWindow specialFeaturesWindow) {
        JButton closeApplicationButton = new JButton();
        closeApplicationButton.setSize(specialFeaturesWindow.getWidth(), specialFeaturesWindow.getHeight() / 4);
        closeApplicationButton.setLocation(0, 0);
        return closeApplicationButton;
    }

    JCheckBox hideUICheckbox(JWindow specialFeaturesWindow) {
        hideUICheckbox = new JCheckBox(" Hide UI ");
        hideUICheckbox.setSize(specialFeaturesWindow.getWidth(), specialFeaturesWindow.getHeight() / 4);
        hideUICheckbox.setLocation(specialFeaturesWindow.getWidth() / 25, specialFeaturesWindow.getHeight() * 1 / 4);
        hideUICheckbox.setFont(new Font(Font.SANS_SERIF,Font.PLAIN, getWidth() / 100));
        hideUICheckbox.setForeground(java.awt.Color.white);
        hideUICheckbox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                saveLoadObjectWindow.setVisible(false);
                saveButtonWindow.setVisible(false);
                mouseKeyWindow.setVisible(false);
                objectWindow.setVisible(false);
                axis.setVisible(false);
            } else {
                saveLoadObjectWindow.setVisible(true);
                saveButtonWindow.setVisible(true);
                mouseKeyWindow.setVisible(true);
                objectWindow.setVisible(true);
                axis.setVisible(true);
            }
        });
        hideUICheckbox.setVisible(true);
        return hideUICheckbox;
    }


    JCheckBox selectObjectMouseCheckbox(JWindow specialFeaturesWindow) {
        selectObjectMouseCheckbox = new JCheckBox(" Click & Drag Objects ");
        selectObjectMouseCheckbox.setSize(specialFeaturesWindow.getWidth(), specialFeaturesWindow.getHeight() / 4);
        selectObjectMouseCheckbox.setLocation(specialFeaturesWindow.getWidth() / 25, specialFeaturesWindow.getHeight() * 1 / 2);
        selectObjectMouseCheckbox.setFont(new Font(Font.SANS_SERIF,Font.PLAIN, getWidth() / 100));
        selectObjectMouseCheckbox.setForeground(java.awt.Color.white);
        selectObjectMouseCheckbox.setVisible(true);
        return selectObjectMouseCheckbox;
    }

    void saveLoadRandomObjectWindow() {

        saveLoadObjectWindow = new JWindow();

        saveLoadObjectWindow.setBackground(new java.awt.Color(0,0,0,0));
        saveLoadObjectWindow.setSize(getWidth() / 4, getHeight() / 8);
        saveLoadObjectWindow.setLocation(getWidth() - saveLoadObjectWindow.getWidth(), getHeight() / 3 + getHeight() / 3);

        saveLoadObjectWindow.setLayout(null);
        saveLoadObjectWindow.setAlwaysOnTop(true);

        JButton createRandomObjectButton = new JButton("Create Random Objects"){
            {
                setSize(saveLoadObjectWindow.getWidth(), saveLoadObjectWindow.getHeight() / 2);
                setLocation(0, 0);

                addActionListener(e -> {

                    JDialog dialog = new JDialog() {{
                            setTitle(" Randomizer ");
                            setSize(Window.this.getWidth( ) / 2, Window.this.getHeight( ) / 2);
                            setLocationRelativeTo(null);
                            setLayout(null);
                            setVisible(true);
                        }};

                    String[] shapeRandomizerChoice = {
                            "Any", "Box", "Prism", "Cone", "Sphere", "Cylinder", "Triangle", "Octahedron"
                    };
                    String[] shapeRandomizerText = {
                            "1 - 5", "1 - 10", "10 - 20", "20 - 50", "50 - 100"
                    };
                    String[] radiusRandomizerText = {
                            "0 - 0.5", "0 - 1", "1 - 2", "2 - 5", "5 - 10", "10 - 20", "20 - 50", "50 - 100"
                    };

                    String[] yRandomizerText = {
                            "0 - 0", "0 - 1", "1 - 2", "2 - 5", "5 - 10", "10 - 20", "20 - 50", "50 - 100"
                    };

                    String[] xRandomizerText = {
                           "0 - 1", "0 - 2", "1 - 5", "1 - 10", "10 - 20", "10 - 50", "10 - 100", "50 - 100"
                    };

                    String[] zRandomizerText = {
                            "0 - 1", "0 - 2", "1 - 5", "1 - 10", "10 - 20", "10 - 50", "10 - 100"
                    };

                    String[] fuzzRandomizerText = {
                            "0.01 - 0.10","0.10 - 0.25", "0.10 - 0.50", "0.10 - 0.75", "0.00 - 1.00", "1.00 - 10.00"
                    };
                    String[] materialRandomizerText = {
                            "ANY", "METAL", "DIELECTRIC", "LAMBERTIAN", "GLOSSY", "PLASTIC", "MATTE", "MIRROR", "TRANSLUCENT", "CHROME", "MAGIC_GOO", "ANODIZED_METAL", "MIST"
                    };

                    String[] selectedShapeRandomizerChoice = new String[1];   selectedShapeRandomizerChoice[0] = "Any";
                    String[] selectedShapeRandomizer = new String[1];   selectedShapeRandomizer[0] = "1 - 5";
                    String[] selectedRadiusRandomizer = new String[1];   selectedRadiusRandomizer[0] = "0 - 0.5";
                    String[] selectedxRandomizer = new String[1];        selectedxRandomizer[0] = "0 - 1";
                    String[] selectedyRandomizer = new String[1];        selectedyRandomizer[0] = "0 - 0";
                    String[] selectedzRandomizer = new String[1];        selectedzRandomizer[0] = "0 - 1";
                    String[] selectedfuzzRandomizer = new String[1];     selectedfuzzRandomizer[0] = "0.01 - 0.10";
                    String[] selectedmaterialRandomizer = new String[1]; selectedmaterialRandomizer[0] = "ANY";

                    final Color[] color = {null};
                    JButton editColorButton = new JButton(" Pick Color ");
                    editColorButton.setForeground(java.awt.Color.black);
                    editColorButton.setOpaque(true); editColorButton.setContentAreaFilled(true);
                    editColorButton.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 25));
                    editColorButton.setSize(dialog.getWidth() / 4, dialog.getHeight() / 16);
                    editColorButton.setLocation( dialog.getWidth() / 2 - editColorButton.getWidth() / 2 - dialog.getWidth() / 18, dialog.getHeight() / 4 + dialog.getHeight() / 26);
                    editColorButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new java.awt.Color(50,50,50)),
                            BorderFactory.createEmptyBorder( 0,0,0,0)
                    ));

                    editColorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    editColorButton.addActionListener(e2 -> {

                        JColorChooser colorChooser = new JColorChooser();
                        colorChooser.setPreviewPanel(new JPanel());

                        JDialog colorDialog = JColorChooser.createDialog(

                                dialog,
                                "Choose One Color",
                                true,
                                colorChooser,
                                e3 -> {

                                    java.awt.Color theChoosenOne = colorChooser.getColor();
                                    color[0] = fromAWTColor(theChoosenOne);

                                    editColorButton.setBackground(theChoosenOne);
                                },
                                null
                        );

                        colorDialog.setSize(dialog.getWidth(), dialog.getHeight());
                        colorDialog.setLocationRelativeTo(null);
                        colorDialog.setVisible(true);

                    });

                    JComboBox<String> shapeRandomizerNumbers = new JComboBox(shapeRandomizerText){{
                        setSize(dialog.getWidth() / 6, dialog.getHeight() / 18);
                        setLocation(dialog.getWidth() / 2 - ( getWidth() ) + getWidth() / 4, dialog.getHeight() * 1 / 10);
                        addActionListener(e -> {
                            selectedShapeRandomizer[0] = (String) getSelectedItem();
                        });
                    }};
                    JComboBox<String> shapeRandomizerChooser =  new JComboBox<>(shapeRandomizerChoice){{
                        setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                        setLocation(( dialog.getWidth() / 2 ) - getWidth() / 2 - getWidth() / 4, ( dialog.getHeight() * 1 / 12 ) - shapeRandomizerNumbers.getHeight());
                        addActionListener(e -> {
                            selectedShapeRandomizerChoice[0] = (String) getSelectedItem();
                        });
                    }};

                    JComboBox<String> radiusRandomizer = new JComboBox(radiusRandomizerText){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 18);
                            setLocation(dialog.getWidth() / 4, dialog.getHeight() / 4 - dialog.getHeight() / 18 );
                            addActionListener(e -> {
                                selectedRadiusRandomizer[0] = (String) getSelectedItem();
                            });
                        }};
                    JLabel radiusRandomizerLabel =  new JLabel("Radius"){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 4 + radiusRandomizer.getWidth() / 4 + radiusRandomizer.getWidth() / 12 , dialog.getHeight() / 4 - radiusRandomizer.getHeight() - dialog.getHeight() / 18 );
                        }};

                    JComboBox<String> fuzzRandomizer = new JComboBox<>(fuzzRandomizerText){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 18);
                            setLocation(dialog.getWidth() / 2, dialog.getHeight() / 4 - dialog.getHeight() / 18);
                            addActionListener(e -> {
                               selectedfuzzRandomizer[0] = (String) getSelectedItem();
                            });
                        }};
                    JLabel fuzzRandomizerLabel =  new JLabel("Fuzz"){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 2 + radiusRandomizer.getWidth() / 4 + radiusRandomizer.getWidth() / 12 , dialog.getHeight() / 4 - radiusRandomizer.getHeight() - dialog.getHeight() / 18);
                        }};

                    JComboBox<String> xRandomizer = new JComboBox(xRandomizerText){{
                            setSize(dialog.getWidth() / 8, dialog.getHeight() / 18);
                            setLocation(dialog.getWidth() / 5, dialog.getHeight() / 2);
                            addActionListener(e -> {
                                selectedxRandomizer[0] = (String) getSelectedItem();
                            });
                        }};
                    JLabel xRandomizerLabel =  new JLabel("X"){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 5 + radiusRandomizer.getWidth() / 4 + radiusRandomizer.getWidth() / 12 , dialog.getHeight() / 2 - radiusRandomizer.getHeight());
                        }};

                    JComboBox<String> yRandomizer = new JComboBox(yRandomizerText){{
                            setSize(dialog.getWidth() / 8, dialog.getHeight() / 18);
                            setLocation(dialog.getWidth() / 5 + dialog.getWidth() / 5, dialog.getHeight() / 2);
                            addActionListener(e -> {
                                selectedyRandomizer[0] = (String) getSelectedItem();
                            });
                        }};
                    JLabel yRandomizerLabel =  new JLabel("Y"){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 5 + dialog.getWidth() / 5 + radiusRandomizer.getWidth() / 4 + radiusRandomizer.getWidth() / 12 , dialog.getHeight() / 2 - radiusRandomizer.getHeight());
                        }};

                    JComboBox<String> zRandomizer = new JComboBox(zRandomizerText){{
                            setSize(dialog.getWidth() / 8, dialog.getHeight() / 18);
                            setLocation( (dialog.getWidth() / 5 ) * 3, dialog.getHeight() / 2);
                            addActionListener(e -> {
                                selectedzRandomizer[0] = (String) getSelectedItem();
                            });
                        }};
                    JLabel zRandomizerLabel =  new JLabel("Z"){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(( ( dialog.getWidth() / 5 ) * 3 ) + radiusRandomizer.getWidth() / 4 + radiusRandomizer.getWidth() / 12 , dialog.getHeight() / 2 - radiusRandomizer.getHeight());
                        }};

                    JComboBox<String> materialRandomizerMenu = new JComboBox<>(materialRandomizerText){{
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 4 , dialog.getHeight() * 3 / 4);
                            addActionListener(e -> {
                                selectedmaterialRandomizer[0] = (String) getSelectedItem();
                            });
                        }};

                    int groundedSphereTrue = 0, avoidCollisionTrue = 0;
                    JCheckBox groundedSpheresCheckBox = new JCheckBox(" Ground Spheres "){{
                            setSize(dialog.getWidth() / 4, dialog.getHeight() / 8);
                            setLocation(dialog.getWidth() / 2, dialog.getHeight() / 3);

                        }};

                    JCheckBox avoidCollisionCheckbox = new JCheckBox(" Avoid Collision "){{
                            setSize(dialog.getWidth() / 4, dialog.getHeight() / 8);
                            setLocation(dialog.getWidth() / 4, dialog.getHeight() / 3);
                        }};

                    dialog.add(groundedSpheresCheckBox);
                    dialog.add(avoidCollisionCheckbox);

                    JProgressBar randomGenerationProgress = new JProgressBar(){
                        {
                            setSize(dialog.getWidth() / 2, dialog.getHeight() / 16);
                            setLocation(dialog.getWidth() / 2 - ( getWidth() / 2 ) - getWidth() / 16, dialog.getHeight() * 3 / 4 - dialog.getWidth() * 1 / 12);
                            setVisible(true);
                        }
                    };

                    JButton generateButton = (JButton) new JButton(" Generate "){{
                            setSize(dialog.getWidth() / 6, dialog.getHeight() / 15);
                            setLocation(dialog.getWidth() / 2, dialog.getHeight() * 3 / 4);

                            addActionListener(e -> {

                                Material materialType = Material.NONE;
                                double radiusMin = 0, radiusMax = 0.25;
                                double fuzzMin = 0.01, fuzzMax = 0.10;
                                double xMin = 0, xMax = 0.25;
                                double yMin = 0, yMax = 0.25;
                                double zMin = 0, zMax = 0.25;
                                int shapeCount = 1, shapeChooser = 0;

                                switch (selectedShapeRandomizerChoice[0]) {
                                    case "Any": shapeChooser = 0; break;
                                    case "Sphere": shapeChooser = 1; break;
                                    case "Box": shapeChooser = 2; break;
                                    case "Cylinder": shapeChooser = 3; break;
                                    case "Triangle": shapeChooser = 4; break;
                                    case "Cone": shapeChooser = 5; break;
                                    case "Prism": shapeChooser = 6; break;
                                    case "Octahedron": shapeChooser = 7; break;
                                }

                                switch (selectedShapeRandomizer[0]) {
                                    case "1 - 5": shapeCount = 1 + random.nextInt(5); break;
                                    case "1 - 10": shapeCount = 1 + random.nextInt(10); break;
                                    case "10 - 20": shapeCount = 10 + random.nextInt(20); break;
                                    case "20 - 50": shapeCount = 20 + random.nextInt(31); break;
                                    case "50 - 100": shapeCount = 50 + random.nextInt(51); break;
                                }
                                switch (selectedmaterialRandomizer[0].toUpperCase()) {
                                    case "ANY": materialType = Material.NONE; break;
                                    case "METAL": materialType = Material.METAL; break;
                                    case "DIELECTRIC": materialType = Material.DIELECTRIC; break;
                                    case "LAMBERTIAN": materialType = Material.LAMBERTIAN; break;
                                    case "GLOSSY": materialType = Material.GLOSSY; break;
                                    case "PLASTIC": materialType = Material.PLASTIC; break;
                                    case "MATTE": materialType = Material.MATTE; break;
                                    case "MIRROR": materialType = Material.MIRROR; break;
                                    case "TRANSLUCENT": materialType = Material.TRANSLUCENT; break;
                                    case "CHROME": materialType = Material.CHROME; break;
                                    case "MAGIC_GOO": materialType = Material.MAGIC_GOO; break;
                                    case "ANODIZED_METAL": materialType = Material.ANODIZED_METAL; break;
                                    case "CRYSTAL": materialType = Material.CRYSTAL; break;
                                    case "MIST": materialType = Material.MIST; break;
                                }

                                switch (selectedRadiusRandomizer[0]) {
                                    case "0 - 0.5": radiusMin = 0; radiusMax = 0.5; break;
                                    case "0 - 1": radiusMin = 0; radiusMax = 1; break;
                                    case "1 - 2": radiusMin = 1; radiusMax = 2; break;
                                    case "2 - 5": radiusMin = 2; radiusMax = 5; break;
                                    case "5 - 10": radiusMin = 5; radiusMax = 10; break;
                                    case "10 - 20": radiusMin = 10; radiusMax = 20; break;
                                    case "20 - 50": radiusMin = 20; radiusMax = 50; break;
                                    case "50 - 100": radiusMin = 50; radiusMax = 100; break;
                                }

                                switch (selectedxRandomizer[0]) {
                                    case "0 - 1": xMin = 0; xMax = 1; break;
                                    case "0 - 2": zMin = 0; zMax = 2; break;
                                    case "1 - 5": xMin = 1; xMax = 5; break;
                                    case "1 - 10": xMin = 1; xMax = 10; break;
                                    case "10 - 20": xMin = 10; xMax = 20; break;
                                    case "10 - 50": xMin = 10; xMax = 50; break;
                                    case "10 - 100": xMin = 10; xMax = 100; break;
                                }

                                switch (selectedyRandomizer[0]) {
                                    case "0 - 0": yMin = 0; yMax = 0; break;
                                    case "0 - 1": yMin = 0; yMax = 1; break;
                                    case "1 - 2": yMin = 1; yMax = 2; break;
                                    case "2 - 5": yMin = 2; yMax = 5; break;
                                    case "5 - 10": yMin = 5; yMax = 10; break;
                                    case "10 - 20": yMin = 10; yMax = 20; break;
                                    case "10 - 50": yMin = 10; yMax = 50; break;
                                }

                                switch (selectedzRandomizer[0]) {
                                    case "0 - 1": zMin = 0; zMax = 1; break;
                                    case "0 - 2": zMin = 0; zMax = 2; break;
                                    case "1 - 5": zMin = 1; zMax = 5; break;
                                    case "1 - 10": zMin = 1; zMax = 10; break;
                                    case "10 - 20": zMin = 10; zMax = 20; break;
                                    case "10 - 50": zMin = 10; zMax = 50; break;
                                    case "10 - 100": zMin = 10; zMax = 100; break;
                                }

                                switch (selectedfuzzRandomizer[0]) {
                                    case "0.01 - 0.10": fuzzMin = 0.01; fuzzMax = 0.10; break;
                                    case "0.10 - 0.25": fuzzMin = 0.10; fuzzMax = 0.25; break;
                                    case "0.10 - 0.50": fuzzMin = 0.10; fuzzMax = 0.50; break;
                                    case "0.10 - 0.75": fuzzMin = 0.10; fuzzMax = 0.75; break;
                                    case "0.00 - 1.00": fuzzMin = 0.00; fuzzMax = 1.00; break;
                                    case "1.00 - 10.00": fuzzMin = 1.00; fuzzMax = 10.00; break;
                                }

                                double finalXMin = xMin;
                                double finalYMin = yMin;
                                double finalZMin = zMin;
                                double finalXMax = xMax;
                                double finalXMin1 = xMin;
                                double finalYMin1 = yMin;
                                double finalYMax = yMax;
                                double finalZMax = zMax;
                                double finalZMin1 = zMin;
                                double finalFuzzMin = fuzzMin;
                                double finalFuzzMax = fuzzMax;
                                double finalFuzzMin1 = fuzzMin;
                                int finalShapeCount = shapeCount;
                                double finalRadiusMin = radiusMin;
                                double finalRadiusMax = radiusMax;
                                double finalRadiusMin1 = radiusMin;
                                int finalShapeChooser = shapeChooser;
                                Material finalMaterialType = materialType;
                                new Thread(() -> {

                                    boolean groundedMode = groundedSpheresCheckBox.isSelected();
                                    boolean avoidCollision = avoidCollisionCheckbox.isSelected();

                                    for (int i = 0; i < finalShapeCount; i++) {

                                        int finalI = i;
                                        SwingUtilities.invokeLater(() -> { randomGenerationProgress.setValue((int) ( finalI / (double) finalShapeCount * 100 ) ); });

                                        int maxTries = 100;
                                        while (maxTries-- > 0) {

                                            double x = finalXMin + random.nextDouble() * (finalXMax - finalXMin1);
                                            double y = finalYMin + random.nextDouble() * (finalYMax - finalYMin1);
                                            double z = finalZMin + random.nextDouble() * (finalZMax - finalZMin1);
                                            double fuzz = finalFuzzMin + random.nextDouble() * (finalFuzzMax - finalFuzzMin1);
                                            double radius = finalRadiusMin + random.nextDouble() * (finalRadiusMax - finalRadiusMin1);
                                            Shape s;

                                            double YfinalChecker = M_Y + y;

                                            if(groundedMode == true){
                                                YfinalChecker = radius;
                                            }

                                            Material materialToUse = (finalMaterialType == Material.NONE)
                                                    ? new Material[]{
                                                    Material.LAMBERTIAN,
                                                    Material.METAL,
                                                    Material.DIELECTRIC,
                                                    Material.GLOSSY,
                                                    Material.MATTE,
                                                    Material.PLASTIC,
                                                    Material.MIRROR,
                                                    Material.TRANSLUCENT,
                                                    Material.CHROME,
                                                    Material.MAGIC_GOO,
                                                    Material.ANODIZED_METAL,
                                                    Material.CRYSTAL,
                                                    Material.MIST,
                                            }[random.nextInt(13)]
                                                    : finalMaterialType;

                                            Color finalColor = (color[0] == null)
                                                    ? new Color(random.nextFloat(), random.nextFloat(), random.nextFloat())
                                                    : color[0];

                                            Point3D center = new Point3D(M_X + x, YfinalChecker, M_Z + z);


                                            if (finalShapeChooser == 1) {  // Sphere
                                                s = new Sphere(center, radius, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 2) {  // Box
                                                double edge = (radius * 2) / Math.sqrt(3);
                                                s = new Box(center, edge, edge, edge, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 3) {  // Cylinder
                                                double height = radius * 2;
                                                s = new Cylinder(center, radius, height, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 4) {  // Triangle
                                                Point3D v0 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                Point3D v1 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                Point3D v2 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                s = new Triangle(v0, v1, v2, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 5) {  // Cone
                                                double coneRadius = radius;
                                                double coneHeight = 2.0 * radius;
                                                s = new Cone(center, coneRadius, coneHeight, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 6) {  // Prism
                                                Point3D min = center.sub(new Point3D(radius, radius, radius));
                                                Point3D max = center.add(new Point3D(radius, radius, radius));
                                                s = new Prism(min, max, finalColor, materialToUse, fuzz);
                                            } else if (finalShapeChooser == 7) {  // Octahedron
                                                s = new Octahedron(center, radius, finalColor, materialToUse, fuzz);
                                            } else {
                                                s = null;
                                                int randomShape = 1 + random.nextInt(7);
                                                switch (randomShape) {
                                                    case 1: // Sphere
                                                        s = new Sphere(center, radius, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 2: // Box
                                                        double edge = (radius * 2) / Math.sqrt(3);
                                                        s = new Box(center, edge, edge, edge, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 3: // Cylinder
                                                        double height = radius * 2;
                                                        s = new Cylinder(center, radius, height, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 4: // Triangle
                                                        Point3D rv0 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                        Point3D rv1 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                        Point3D rv2 = center.add(new Point3D(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(radius));
                                                        s = new Triangle(rv0, rv1, rv2, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 5: // Cone
                                                        double coneRadius = radius;
                                                        double coneHeight = 2.0 * radius;
                                                        s = new Cone(center, coneRadius, coneHeight, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 6: // Prism
                                                        Point3D min = center.sub(new Point3D(radius, radius, radius));
                                                        Point3D max = center.add(new Point3D(radius, radius, radius));
                                                        s = new Prism(min, max, finalColor, materialToUse, fuzz);
                                                        break;
                                                    case 7: // Octahedron
                                                        s = new Octahedron(center, radius, finalColor, materialToUse, fuzz);
                                                        break;
                                                }
                                            }

                                            if(avoidCollision == true){
                                                if (Main.doesCollide(s, WORLD) == false) {
                                                    WORLD.add(s);
                                                    break;
                                                }
                                            } else {
                                                WORLD.add(s);
                                                break;
                                            }

                                        }
                                    }

                                    refreshShapeButtons();
                                    drawImage(160, 320, ANTI_ALISING_SAMPLES);

                                }).start();

                            });
                        }};

                    dialog.add(xRandomizer);
                    dialog.add(yRandomizer);
                    dialog.add(zRandomizer);
                    dialog.add(generateButton);
                    dialog.add(fuzzRandomizer);
                    dialog.add(editColorButton);
                    dialog.add(radiusRandomizer);
                    dialog.add(xRandomizerLabel);
                    dialog.add(yRandomizerLabel);
                    dialog.add(zRandomizerLabel);
                    dialog.add(fuzzRandomizerLabel);
                    dialog.add(radiusRandomizerLabel);
                    dialog.add(shapeRandomizerChooser);
                    dialog.add(shapeRandomizerNumbers);
                    dialog.add(materialRandomizerMenu);
                    dialog.add(randomGenerationProgress);
                });
            }
        };

        saveLoadObjectWindow.add(createRandomObjectButton);
        saveLoadObjectWindow.add(new JButton("Save Objects"){
            {
                setSize(saveLoadObjectWindow.getWidth() / 2, saveLoadObjectWindow.getHeight() / 2);
                setLocation(saveLoadObjectWindow.getWidth() / 2, saveLoadObjectWindow.getHeight() / 2);
                addActionListener(e -> {
                    new JFileChooser(){
                        {
                            setDialogTitle("Save Objects");
                            setFileFilter(new FileNameExtensionFilter("Serialized World Files (*.ser)", "ser"));
                            if(showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                                File selectedFile = getSelectedFile();
                                if (!selectedFile.getName().toLowerCase().endsWith(".ser")) {
                                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".ser");
                                }
                                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                                    out.writeObject(WORLD);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    };

                });

            }
        });

        saveLoadObjectWindow.add(new JButton("Load Objects"){
            {
                setSize(saveLoadObjectWindow.getWidth() / 2, saveLoadObjectWindow.getHeight() / 2);
                setLocation(0, saveLoadObjectWindow.getHeight() / 2);
                addActionListener(e -> {
                    new JFileChooser(){
                        {
                            setDialogTitle("Load Objects");
                            setFileFilter(new FileNameExtensionFilter("Serialized World Files (*.ser)", "ser"));
                            if(showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getSelectedFile()))) {
                                    Object obj = in.readObject();
                                    if(obj instanceof ArrayList){
                                        ArrayList<?> newWorld = (ArrayList<?>) obj;
                                        if (!newWorld.isEmpty() && newWorld.get(0) instanceof Shape) {
                                            WORLD = (ArrayList<Shape>) newWorld;
                                            refreshShapeButtons();
                                            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                                        }
                                    }
                                } catch (IOException | ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    };

                });

            }
        });

        saveLoadObjectWindow.setVisible(true);
    }

    void saveImageWindow(){

        saveButtonWindow = new JWindow();

        saveButtonWindow.setBackground(new java.awt.Color(0,0,0,0));
        saveButtonWindow.setSize(getWidth() / 4, getHeight() / 8);
        saveButtonWindow.setLocation(( getWidth() / 2 )  - saveButtonWindow.getWidth() / 2, getHeight() - saveButtonWindow.getHeight());

        saveButtonWindow.setLayout(null);
        saveButtonWindow.setAlwaysOnTop(true);

        saveButtonWindow.add(saveImageButton(saveButtonWindow));
        saveButtonWindow.setVisible(true);
    }

    JButton saveImageButton(JWindow saveButtonWindow) {

        JButton saveButton = new JButton( "Save Image" );
        saveButton.setSize(saveButtonWindow.getWidth() / 2, saveButtonWindow.getHeight() / 3);
        saveButton.setLocation( (saveButtonWindow.getWidth() / 2) - (saveButton.getWidth() / 2),saveButtonWindow.getHeight() / 2 );
        saveButton.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> {

            JDialog saveButtonMenu = new JDialog(this, "Save Image", true, null);
            saveButtonMenu.setSize(getWidth() / 2, getHeight() / 2);
            saveButtonMenu.setLocationRelativeTo(null);
            saveButtonMenu.setLayout(null);

            String[] resolutions = {
                    "1280 x 720",     // HD
                    "1920 x 1080",    // Full HD
                    "2560 x 1440",    // 2K QHD
                    "3840 x 2160",    // 4K UHD
                    "7680 x 4320",    // 8K UHD
                    "15360 x 8640",   // 16K UHD
                    "30720 x 17280"   // 32K UHD
            };

            String[] samples = {
                    "1",
                    "10",
                    "50",
                    "100",
                    "200",
                    "400",
                    "800",
            };

            String[] selectedResolution = new String[1];
            selectedResolution[0] = "1280 x 720";

            String[] selectedSample = new String[1];
            selectedSample[0] = "1";

            saveImageProgress = new JProgressBar(){

                {
                    setSize(saveButtonMenu.getWidth() / 2, saveButtonMenu.getHeight() / 10);
                    setLocation(saveButtonMenu.getWidth() / 2 - ( saveButtonMenu.getWidth() / 4 ) , saveButtonMenu.getHeight() * 3 / 4);
                    setVisible(false);
                }

            };

            saveButtonMenu.add(saveImageProgress);

            saveButtonMenu.add(new JComboBox<String>(resolutions){

                {
                    addActionListener(e -> {
                        selectedResolution[0] = (String) getSelectedItem();
                    });

                    setSize(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 8);
                    setLocation(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 4);
                }

            });

            saveButtonMenu.add(new JComboBox<String>(samples){

                {
                    addActionListener(e -> {
                        selectedSample[0] = (String) getSelectedItem();
                    });

                    setSize(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 8);
                    setLocation(saveButtonMenu.getWidth() / 2, saveButtonMenu.getHeight() / 4);
                }

            });

            saveButtonMenu.add(new JButton(" Save "){

                {
                    addActionListener(e -> {

                        int rHeight = 0, rWidth = 0;
                        switch (selectedResolution[0]) {
                            case "1280 x 720":  rWidth = 1280; rHeight = 720; break;
                            case "1920 x 1080": rWidth = 1920; rHeight = 1080; break;
                            case "2560 x 1440": rWidth = 2560; rHeight = 1440; break;
                            case "3840 x 2160": rWidth = 3840; rHeight = 2160; break;
                            case "7680 x 4320": rWidth = 7680; rHeight = 4320; break;
                            case "15360 x 8640": rWidth = 15360; rHeight = 8640; break;
                            case "30720 x 17280": rWidth = 30720; rHeight = 17280; break;
                        }

                        int samples = 0;
                        switch(selectedSample[0]){
                            case "1": samples = 1; break;
                            case "10": samples = 10; break;
                            case "50": samples = 50; break;
                            case "100": samples = 100; break;
                            case "200": samples = 200; break;
                            case "400": samples = 400; break;
                            case "800": samples = 800; break;
                        }
                        saveImageProgress.setVisible(true);

                        JFileChooser theChoosenOne = new JFileChooser();

                        theChoosenOne.setDialogTitle(" Save Image ");
                        theChoosenOne.setFileFilter(new FileNameExtensionFilter("Image File (*.jpg)", "jpg"));

                        if(theChoosenOne.showSaveDialog(saveButtonMenu) == JFileChooser.APPROVE_OPTION){

                            File theChoosenFile = theChoosenOne.getSelectedFile();

                            if (!theChoosenFile.getName().toLowerCase().endsWith(".jpg")) {
                                theChoosenFile = new File(theChoosenFile.getParentFile(), theChoosenFile.getName() + ".jpg");
                            }

                            int finalRHeight = rHeight, finalRWidth = rWidth, finalSamples = samples;
                            File finalTheChoosenFile = theChoosenFile;
                            new Thread(() -> {
                                    {
                                        saveImage(finalRHeight, finalRWidth, finalSamples);
                                        try {
                                            ImageIO.write(SAVEDIMAGE, "jpg", finalTheChoosenFile);
                                            Desktop.getDesktop( ).open(finalTheChoosenFile);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                }).start();
                        }
                    });

                    setSize(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 12);
                    setLocation(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 2 );
                }

            });

            saveButtonMenu.add(new JButton(" Cancel "){

                {
                    addActionListener(e -> {
                        saveButtonMenu.dispose();
                    });

                    setSize(saveButtonMenu.getWidth() / 4, saveButtonMenu.getHeight() / 12);
                    setLocation(saveButtonMenu.getWidth() / 2, saveButtonMenu.getHeight() / 2 );
                }

            });

            saveButtonMenu.setVisible(true);
        });

        return saveButton;
    }

    void saveImage(int HEIGHT, int WIDTH, int ANTI_ALISING_SAMPLES) {
        newCameraPosition();
        BEINGRENDERED = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        double u, v; Ray ray;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Color pixelColorSum = new Color(0, 0, 0);
                for (int s = 0; s < ANTI_ALISING_SAMPLES; s++) {
                    u = (double) (x + Math.random( )) / WIDTH;
                    v = (double) (HEIGHT - y + Math.random( )) / HEIGHT;
                    ray = CAMERA.getRay(u, v);
                    pixelColor = new Color(Main.rayColor(WORLD, ray, environmentMap, 10));
                    pixelColorSum.addColors(pixelColor);
                }
                pixelColorSum.divideColors(ANTI_ALISING_SAMPLES);
                BEINGRENDERED.setRGB(x, y, pixelColorSum.colorToInteger( ));
            }
            int finalY = y;
            SwingUtilities.invokeLater(() -> { saveImageProgress.setValue((int) ( finalY / (double) HEIGHT * 100 ) ); });
        }

        SAVEDIMAGE = BEINGRENDERED;

        repaint();
    }


    void objectWindow() {

        objectWindow = new JWindow();

        objectWindow.setBackground(new java.awt.Color(0,0,0,0));
        objectWindow.setSize(getWidth() / 8, getHeight() / 4);
        objectWindow.setLocation(
                getWidth() - objectWindow.getWidth(),
                getHeight() / 2 - getHeight() / 8
        );

        objectWindow.setAlwaysOnTop(true);
        objectWindow.setLayout( null );

        objectWindow.add(viewObjects(objectWindow));

        objectWindow.setVisible(true);
    }

    JScrollPane viewObjects(JWindow objectWindow){

        viewObjects = new JPanel();
        viewObjects.setBackground(new java.awt.Color(10, 10, 10));
        viewObjects.setSize(objectWindow.getWidth(), objectWindow.getHeight());
        viewObjects.setLayout(new BoxLayout(viewObjects, BoxLayout.Y_AXIS));

        for(int i = 0; i < WORLD.size(); i++){

            Shape shape = WORLD.get(i);

            JButton btn = new JButton();
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE ));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new java.awt.Color(0,0,0), 0),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            viewObjects.add(btn); objectCounter++;
            viewObjects.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));

            btn.setActionCommand(String.valueOf(i));

            btn.addActionListener(e -> {

                if (shape instanceof Sphere sphere) {
                    btn.setText("Sphere " + WORLD.indexOf(sphere));
                    editObjectWindow(sphere, WORLD.indexOf(sphere), btn);

                } else if (shape instanceof Triangle triangle) {
                    btn.setText("Triangle " + WORLD.indexOf(triangle));
                    editObjectWindow(triangle, WORLD.indexOf(triangle), btn);

                } else if (shape instanceof Box box) {
                    btn.setText("Box " + WORLD.indexOf(box));
                    editObjectWindow(box, WORLD.indexOf(box), btn);

                } else if (shape instanceof Cylinder cylinder) {
                    btn.setText("Cylinder " + WORLD.indexOf(cylinder));
                    editObjectWindow(cylinder, WORLD.indexOf(cylinder), btn);

                } else if (shape instanceof Cone cone) {
                    btn.setText("Cone " + WORLD.indexOf(cone));
                    editObjectWindow(cone, WORLD.indexOf(cone), btn);

                } else if (shape instanceof Prism prism) {
                    btn.setText("Prism " + WORLD.indexOf(prism));
                    editObjectWindow(prism, WORLD.indexOf(prism), btn);

                } else if (shape instanceof Octahedron octahedron) {
                    btn.setText("Octahedron " + WORLD.indexOf(octahedron));
                    editObjectWindow(octahedron, WORLD.indexOf(octahedron), btn);
                }

            });
        }

        viewObjectsScrolling = new JScrollPane(
                viewObjects,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        viewObjectsScrolling.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        viewObjectsScrolling.setSize(objectWindow.getWidth(), objectWindow.getHeight());
        viewObjectsScrolling.setBackground(new java.awt.Color(0,0,0, 0));
        viewObjectsScrolling.setLocation(0,0);

        viewObjectsScrolling.setBorder( null );
        viewObjectsScrolling.setVisible(true);

        viewObjects.setVisible(true);

        return viewObjectsScrolling;

    }

    void editObjectWindow(Shape s, int shape_name, JButton btn) {

        if(olderEditObjectWindow != null){
            olderEditObjectWindow.dispose();
        }

        JDialog editObjectWindow = new JDialog();

        editObjectWindow.setUndecorated(true);
        editObjectWindow.setBackground(new java.awt.Color(0,0,0,0));
        editObjectWindow.setSize(getWidth() / 4, getHeight() / 4);
        editObjectWindow.setLocation(
                getWidth() - editObjectWindow.getWidth(),
                getHeight() / 10
        );
        editObjectWindow.setAlwaysOnTop(true);
        editObjectWindow.setLayout( null );

        JLabel shapeName = new JLabel();

        if (s instanceof Sphere sphere) {
            shapeName.setText("Sphere " + WORLD.indexOf(s));

        } else if (s instanceof Triangle triangle) {
            shapeName.setText("Triangle " + WORLD.indexOf(s));

        } else if (s instanceof Box box) {
            shapeName.setText("Box " + WORLD.indexOf(s));

        } else if (s instanceof Cylinder cylinder) {
            shapeName.setText("Cylinder " + WORLD.indexOf(s));

        } else if (s instanceof Cone cone) {
            shapeName.setText("Cone " + WORLD.indexOf(s));

        } else if (s instanceof Prism prism) {
            shapeName.setText("Prism " + WORLD.indexOf(s));

        } else if (s instanceof Octahedron octahedron) {
            shapeName.setText("Octahedron " + WORLD.indexOf(s));
        }


        shapeName.setFont(new Font(Font.SERIF, Font.BOLD, getWidth() / 75));
        shapeName.setForeground(java.awt.Color.white);
        shapeName.setBounds(
                ( editObjectWindow.getWidth() / 2 ) - ( getWidth() / 50 ),
                0,
                editObjectWindow.getWidth( ),
                editObjectWindow.getHeight() / 8
        );

        editObjectWindow.add(shapeName);
        editObjectWindow.add(editRadius(editObjectWindow, s));
        editObjectWindow.add(editXPosition(editObjectWindow, s));
        editObjectWindow.add(editYPosition(editObjectWindow, s));
        editObjectWindow.add(editZPosition(editObjectWindow, s));
        editObjectWindow.add(editMaterial(editObjectWindow, s));
        editObjectWindow.add(editFuzz(editObjectWindow, s));
        editObjectWindow.add(editColorButton(editObjectWindow, s));
        editObjectWindow.add(deleteObject(editObjectWindow, s, shape_name, btn));
        editObjectWindow.add(createObject(editObjectWindow, s, shape_name));

        editObjectWindow.setVisible(true);

        olderEditObjectWindow = editObjectWindow;

    }

    JButton createObject(JDialog editObjectWindow, Shape s, int sphere_name) {
        JButton createObject = new JButton(" Add Shape ");
        createObject.setForeground(java.awt.Color.white);
        createObject.setBackground(new java.awt.Color(0,0,0,0));
        createObject.setOpaque(true); createObject.setContentAreaFilled(true);
        createObject.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getWidth() /100));
        createObject.setSize(editObjectWindow.getWidth() / 2, editObjectWindow.getHeight() / 10);
        createObject.setLocation( 0, createObject.getHeight() * 9);
        createObject.setBorder(BorderFactory.createEmptyBorder( 0,0 , 0,0 ));
        createObject.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        createObject.addActionListener(e -> {

            if(s instanceof Sphere sphere){

                Sphere newSphere = new Sphere(
                        new Point3D(M_X, M_Y, M_Z),
                        sphere.radius,
                        new Color(sphere.color.r, sphere.color.g, sphere.color.b),
                        sphere.material,
                        sphere.fuzz
                );

                WORLD.add(newSphere);

            } else if(s instanceof Triangle triangle){

                double cx = triangle.getCentroidX();
                double cy = triangle.getCentroidY();
                double cz = triangle.getCentroidZ();

                double dx = M_X - cx;
                double dy = M_Y - cy;
                double dz = M_Z - cz;

                Point3D newV0 = new Point3D(triangle.v0.x + dx, triangle.v0.y + dy, triangle.v0.z + dz);
                Point3D newV1 = new Point3D(triangle.v1.x + dx, triangle.v1.y + dy, triangle.v1.z + dz);
                Point3D newV2 = new Point3D(triangle.v2.x + dx, triangle.v2.y + dy, triangle.v2.z + dz);

                Triangle newTriangle = new Triangle(
                        newV0, newV1, newV2,
                        new Color(triangle.color.r, triangle.color.g, triangle.color.b),
                        triangle.material,
                        triangle.fuzz
                );

                WORLD.add(newTriangle);
            }else if (s instanceof Box box) {
                // Calculate the current center of the box
                Point3D center = box.getCenter();

                // Calculate offset from center to new position
                double dx = M_X - center.x;
                double dy = M_Y - center.y;
                double dz = M_Z - center.z;

                Point3D newCenter = box.getCenter().add(new Point3D(dx, dy, dz));

                Box newBox = new Box(
                        newCenter,
                        box.getWidth(),
                        box.getHeight(),
                        box.getDepth(),
                        new Color(box.color.r, box.color.g, box.color.b),
                        box.material,
                        box.fuzz
                );


                WORLD.add(newBox);

            } else if (s instanceof Cylinder cylinder) {
                // Calculate the current base center of the cylinder
                Point3D baseCenter = cylinder.center;

                // Calculate offset from base center to new position
                double dx = M_X - baseCenter.x;
                double dy = M_Y - baseCenter.y;
                double dz = M_Z - baseCenter.z;

                Point3D newBaseCenter = new Point3D(baseCenter.x + dx, baseCenter.y + dy, baseCenter.z + dz);

                Cylinder newCylinder = new Cylinder(
                        newBaseCenter,
                        cylinder.radius,
                        cylinder.height,
                        new Color(cylinder.color.r, cylinder.color.g, cylinder.color.b),
                        cylinder.material,
                        cylinder.fuzz
                );

                WORLD.add(newCylinder);
            } else if (s instanceof Cone cone) {

                Point3D center = cone.getCenter();
                double dx = M_X - center.x;
                double dy = M_Y - center.y;
                double dz = M_Z - center.z;

                Point3D newCenter = center.add(new Point3D(dx, dy, dz));

                Cone newCone = new Cone(
                        newCenter,
                        cone.getRadius(),       // base radius
                        cone.getHeight(),       // height
                        new Color(cone.color.r, cone.color.g, cone.color.b),
                        cone.material,
                        cone.fuzz
                );

                WORLD.add(newCone);

            } else if (s instanceof Prism prism) {

                Point3D min = prism.getMin();
                Point3D max = prism.getMax();

                double cx = (min.x + max.x) / 2.0;
                double cy = (min.y + max.y) / 2.0;
                double cz = (min.z + max.z) / 2.0;

                double dx = M_X - cx;
                double dy = M_Y - cy;
                double dz = M_Z - cz;

                Point3D newMin = new Point3D(min.x + dx, min.y + dy, min.z + dz);
                Point3D newMax = new Point3D(max.x + dx, max.y + dy, max.z + dz);

                Prism newPrism = new Prism(
                        newMin,
                        newMax,
                        new Color(prism.color.r, prism.color.g, prism.color.b),
                        prism.material,
                        prism.fuzz
                );

                WORLD.add(newPrism);

            } else if (s instanceof Octahedron octahedron) {

                Point3D center = octahedron.getCenter();
                double dx = M_X - center.x;
                double dy = M_Y - center.y;
                double dz = M_Z - center.z;

                Point3D newCenter = center.add(new Point3D(dx, dy, dz));

                Octahedron newOctahedron = new Octahedron(
                        newCenter,
                        octahedron.getSize(),
                        new Color(octahedron.color.r, octahedron.color.g, octahedron.color.b),
                        octahedron.material,
                        octahedron.fuzz
                );

                WORLD.add(newOctahedron);
            }

            objectCounter++;
            int newIndex = objectCounter - 1;

            JButton newBtn = new JButton();

            if(s instanceof Sphere sphere){
                newBtn.setText("Sphere " + WORLD.indexOf(s));
            } else if(s instanceof Triangle triangle){
                newBtn.setText("Triangle " + WORLD.indexOf(s));
            } else if(s instanceof Box box){
                newBtn.setText("Box " + WORLD.indexOf(s));
            } else if(s instanceof Cylinder cylinder){
                newBtn.setText("Cylinder " + WORLD.indexOf(s));
            } else if(s instanceof Prism prism){
                newBtn.setText("Prism " + WORLD.indexOf(s));
            } else if(s instanceof Cone cone){
                newBtn.setText("Cone " + WORLD.indexOf(s));
            } else if(s instanceof Octahedron octahedron){
                newBtn.setText("Octahedron " + WORLD.indexOf(s));
            }
            newBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            newBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            newBtn.setActionCommand(String.valueOf(newIndex));

            newBtn.addActionListener(evt -> {

                if(s instanceof Sphere sphere){
                    newBtn.setText("Sphere " + WORLD.indexOf(s));
                } else if(s instanceof Triangle triangle){
                    newBtn.setText("Triangle " + WORLD.indexOf(s));
                } else if(s instanceof Box box){
                    newBtn.setText("Box " + WORLD.indexOf(s));
                } else if(s instanceof Cylinder cylinder){
                    newBtn.setText("Cylinder " + WORLD.indexOf(s));
                } else if(s instanceof Prism prism){
                    newBtn.setText("Prism " + WORLD.indexOf(s));
                } else if(s instanceof Cone cone){
                    newBtn.setText("Cone " + WORLD.indexOf(s));
                } else if(s instanceof Octahedron octahedron){
                    newBtn.setText("Octahedron " + WORLD.indexOf(s));
                }


                editObjectWindow(s, WORLD.indexOf(s), newBtn);

            });
            refreshShapeButtons();
            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
            editObjectWindow(s, WORLD.indexOf(s), newBtn);
        });

        createObject.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                createObject.setBackground(new java.awt.Color(255,255,255,100));
                createObject.setForeground(java.awt.Color.BLACK);
            }
            public void mouseExited(MouseEvent e) {
                createObject.setBackground(new java.awt.Color(0,0,0,0));
                createObject.setForeground(java.awt.Color.white);
            }
        });

        return createObject;

    }

    JButton deleteObject(JDialog editObjectWindow, Shape s, int sphere_name, JButton btn) {
        JButton deleteObject = new JButton(" Delete Shape ");
        deleteObject.setForeground(java.awt.Color.white);
        deleteObject.setBackground(new java.awt.Color(0,0,0,0));
        deleteObject.setOpaque(true); deleteObject.setContentAreaFilled(true);
        deleteObject.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getWidth() / 100));
        deleteObject.setSize(editObjectWindow.getWidth() / 2, editObjectWindow.getHeight() / 10);
        deleteObject.setLocation( editObjectWindow.getWidth() - deleteObject.getWidth(), deleteObject.getHeight() * 9);
        deleteObject.setBorder(BorderFactory.createEmptyBorder( 0,0 , 0,0 ));
        deleteObject.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteObject.addActionListener(e -> {
            WORLD.remove(s);
            refreshShapeButtons();
            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
            editObjectWindow.setVisible(false);
        });
        deleteObject.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                deleteObject.setBackground(new java.awt.Color(255,255,255,100));
                deleteObject.setForeground(java.awt.Color.BLACK);
            }
            public void mouseExited(MouseEvent e) {
                deleteObject.setBackground(new java.awt.Color(0,0,0,0));
                deleteObject.setForeground(java.awt.Color.white);
            }
        });
        return deleteObject;
    }

    void refreshShapeButtons() {
        viewObjects.removeAll();
        for (int i = 0; i < WORLD.size(); i++) {

            Shape s = WORLD.get(i);
            JButton btn = new JButton();

            if(s instanceof Sphere sphere){
                btn.setText("Sphere " + i);
            } else if(s instanceof Triangle triangle){
                btn.setText("Triangle " + i);
            } else if(s instanceof Box box){
                btn.setText("Box " + i);
            } else if(s instanceof Cylinder cylinder){
                btn.setText("Cylinder " + i);
            } else if(s instanceof Prism prism){
                btn.setText("Prism " + i);
            } else if(s instanceof Cone cone){
                btn.setText("Cone " + i);
            } else if(s instanceof Octahedron octahedron){
                btn.setText("Octahedron " + i);
            }

            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            btn.setBackground(new java.awt.Color(0, 0, 0));
            btn.setForeground(java.awt.Color.WHITE);

            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            btn.addActionListener(e -> {

                if(s instanceof Sphere sphere){
                    btn.setText("Sphere " + WORLD.indexOf(s));
                } else if(s instanceof Triangle triangle){
                    btn.setText("Triangle " + WORLD.indexOf(s));
                } else if(s instanceof Box box){
                    btn.setText("Box " + WORLD.indexOf(s));
                } else if(s instanceof Cylinder cylinder){
                    btn.setText("Cylinder " + WORLD.indexOf(s));
                } else if(s instanceof Prism prism){
                    btn.setText("Prism " + WORLD.indexOf(s));
                } else if(s instanceof Cone cone){
                    btn.setText("Cone " + WORLD.indexOf(s));
                } else if(s instanceof Octahedron octahedron){
                    btn.setText("Octahedron " + WORLD.indexOf(s));
                }

                editObjectWindow( s, WORLD.indexOf(s), btn);

            });

            viewObjects.add(btn);
            viewObjects.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));

            shapeButtonMap.put( s, btn);

        }
        viewObjects.revalidate();
        viewObjects.repaint();
    }


    JButton editColorButton(JDialog editObjectWindow, Shape s) {

        JButton editColorButton = new JButton(" Pick Color ");
        editColorButton.setForeground(java.awt.Color.white);
        editColorButton.setOpaque(true); editColorButton.setContentAreaFilled(true);
        editColorButton.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        editColorButton.setSize(editObjectWindow.getWidth() / 4, editObjectWindow.getHeight() / 10);
        editColorButton.setLocation( editColorButton.getWidth() - editColorButton.getWidth() / 8, editColorButton.getHeight() * 3);
        editColorButton.setBorder(BorderFactory.createEmptyBorder( 0,0 , 0,0 ));
        editColorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if(s instanceof Sphere sphere){
            editColorButton.setBackground(toAWTColor(sphere.color));
        } else if(s instanceof Triangle triangle) {
            editColorButton.setBackground(toAWTColor(triangle.color));
        } else if(s instanceof Box box) {
            editColorButton.setBackground(toAWTColor(box.color));
        } else if(s instanceof Cylinder cylinder) {
            editColorButton.setBackground(toAWTColor(cylinder.color));
        } else if(s instanceof Prism prism) {
            editColorButton.setBackground(toAWTColor(prism.color));
        } else if(s instanceof Cone cone) {
            editColorButton.setBackground(toAWTColor(cone.color));
        } else if(s instanceof Octahedron octahedron) {
            editColorButton.setBackground(toAWTColor(octahedron.color));
        }


        editColorButton.addActionListener(e -> {

            JColorChooser colorChooser = new JColorChooser();
            colorChooser.setPreviewPanel(new JPanel());

            if(s instanceof Sphere sphere){
                colorChooser.setBackground(toAWTColor(sphere.color));
            } else if(s instanceof Triangle triangle) {
                colorChooser.setBackground(toAWTColor(triangle.color));
            } else if(s instanceof Box box) {
                editColorButton.setBackground(toAWTColor(box.color));
            } else if(s instanceof Cylinder cylinder) {
                editColorButton.setBackground(toAWTColor(cylinder.color));
            } else if(s instanceof Prism prism) {
                editColorButton.setBackground(toAWTColor(prism.color));
            } else if(s instanceof Cone cone) {
                editColorButton.setBackground(toAWTColor(cone.color));
            } else if(s instanceof Octahedron octahedron) {
                editColorButton.setBackground(toAWTColor(octahedron.color));
            }

            JDialog colorDialog = JColorChooser.createDialog(

                    editObjectWindow,
                    "Choose Shape Color",
                    true,
                    colorChooser,
                    e2 -> {
                        java.awt.Color theChoosenOne = colorChooser.getColor();

                        if(s instanceof Sphere sphere){
                            sphere.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Triangle triangle) {
                            triangle.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Box box) {
                            box.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Cylinder cylinder) {
                            cylinder.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Prism prism) {
                            prism.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Cone cone) {
                            cone.color = fromAWTColor(theChoosenOne);
                        } else if(s instanceof Octahedron octahedron) {
                            octahedron.color = fromAWTColor(theChoosenOne);
                        }

                        editColorButton.setBackground(theChoosenOne);

                            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                    },
                    null

            );

            colorDialog.setSize(editObjectWindow.getWidth(), editObjectWindow.getHeight());
            colorDialog.setLocationRelativeTo(editObjectWindow);
            colorDialog.setVisible(true);

        });

        return editColorButton;
    }

    static java.awt.Color toAWTColor(Color c) {
        return new java.awt.Color(
                Math.min(1.0f, Math.max(0.0f, c.r)),
                Math.min(1.0f, Math.max(0.0f, c.g)),
                Math.min(1.0f, Math.max(0.0f, c.b))
        );
    }

    static Color fromAWTColor(java.awt.Color awtColor) {
        return new Color(
                awtColor.getRed() / 255.0f,
                awtColor.getGreen() / 255.0f,
                awtColor.getBlue() / 255.0f
        );
    }


    JPanel editMaterial(JDialog editObjectWindow, Shape s) {
        JPanel editMaterial = new JPanel();
        editMaterial.setOpaque(false);
        editMaterial.setBackground(new java.awt.Color(0,0,0,0));
        editMaterial.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editMaterial.setLocation(editMaterial.getWidth() / 2 , editMaterial.getHeight() * 4);
        editMaterial.setBorder(BorderFactory.createEmptyBorder(0, 0 , 0, ( getWidth() / 100 )));
        editMaterial.setLayout(new BoxLayout(editMaterial, BoxLayout.X_AXIS));

        String[] materials = {
                "NONE",
                "METAL",
                "DIELECTRIC",
                "LAMBERTIAN",
                "GLOSSY",
                "PLASTIC",
                "MATTE",
                "MIRROR",
                "TRANSLUCENT",
                "CHROME",
                "MAGIC_GOO",
                "ANODIZED_METAL",
                "MIST"
        };

        JComboBox<String> editMaterialMenu = new JComboBox<>(materials);
        editMaterialMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if(s instanceof Sphere sphere){
            switch (sphere.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        } else if(s instanceof Triangle triangle) {
            switch (triangle.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        } else if(s instanceof Box box) {
            switch (box.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        } else if(s instanceof Cylinder cylinder) {
            switch (cylinder.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        } else if(s instanceof Prism p) {
            switch (p.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        }
        else if(s instanceof Cone t) {
            switch (t.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        }
        else if(s instanceof Octahedron o) {
            switch (o.material) {
                case NONE: editMaterialMenu.setSelectedItem("NONE"); break;
                case METAL: editMaterialMenu.setSelectedItem("METAL"); break;
                case DIELECTRIC: editMaterialMenu.setSelectedItem("DIELECTRIC"); break;
                case LAMBERTIAN: editMaterialMenu.setSelectedItem("LAMBERTIAN"); break;
                case GLOSSY: editMaterialMenu.setSelectedItem("GLOSSY"); break;
                case PLASTIC: editMaterialMenu.setSelectedItem("PLASTIC"); break;
                case MATTE: editMaterialMenu.setSelectedItem("MATTE"); break;
                case MIRROR: editMaterialMenu.setSelectedItem("MIRROR"); break;
                case TRANSLUCENT: editMaterialMenu.setSelectedItem("TRANSLUCENT"); break;
                case CHROME: editMaterialMenu.setSelectedItem("CHROME"); break;
                case MAGIC_GOO: editMaterialMenu.setSelectedItem("MAGIC_GOO"); break;
                case ANODIZED_METAL: editMaterialMenu.setSelectedItem("ANODIZED_METAL"); break;
                case CRYSTAL: editMaterialMenu.setSelectedItem("CRYSTAL"); break;
                case MIST: editMaterialMenu.setSelectedItem("MIST"); break;
            }
        }


        editMaterialMenu.addActionListener(e -> {

            String theChoosenOne = (String) editMaterialMenu.getSelectedItem();

            if (s instanceof Sphere sphere) {
                switch (theChoosenOne) {
                    case "NONE": sphere.material = Material.NONE; break;
                    case "METAL": sphere.material = Material.METAL; break;
                    case "DIELECTRIC": sphere.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": sphere.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": sphere.material = Material.GLOSSY; break;
                    case "PLASTIC": sphere.material = Material.PLASTIC; break;
                    case "MATTE": sphere.material = Material.MATTE; break;
                    case "MIRROR": sphere.material = Material.MIRROR; break;
                    case "TRANSLUCENT": sphere.material = Material.TRANSLUCENT; break;
                    case "CHROME": sphere.material = Material.CHROME; break;
                    case "MAGIC_GOO": sphere.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": sphere.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": sphere.material = Material.CRYSTAL; break;
                    case "MIST": sphere.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Triangle triangle) {
                switch (theChoosenOne) {
                    case "NONE": triangle.material = Material.NONE; break;
                    case "METAL": triangle.material = Material.METAL; break;
                    case "DIELECTRIC": triangle.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": triangle.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": triangle.material = Material.GLOSSY; break;
                    case "PLASTIC": triangle.material = Material.PLASTIC; break;
                    case "MATTE": triangle.material = Material.MATTE; break;
                    case "MIRROR": triangle.material = Material.MIRROR; break;
                    case "TRANSLUCENT": triangle.material = Material.TRANSLUCENT; break;
                    case "CHROME": triangle.material = Material.CHROME; break;
                    case "MAGIC_GOO": triangle.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": triangle.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": triangle.material = Material.CRYSTAL; break;
                    case "MIST": triangle.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Box box) {
                switch (theChoosenOne) {
                    case "NONE": box.material = Material.NONE; break;
                    case "METAL": box.material = Material.METAL; break;
                    case "DIELECTRIC": box.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": box.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": box.material = Material.GLOSSY; break;
                    case "PLASTIC": box.material = Material.PLASTIC; break;
                    case "MATTE": box.material = Material.MATTE; break;
                    case "MIRROR": box.material = Material.MIRROR; break;
                    case "TRANSLUCENT": box.material = Material.TRANSLUCENT; break;
                    case "CHROME": box.material = Material.CHROME; break;
                    case "MAGIC_GOO": box.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": box.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": box.material = Material.CRYSTAL; break;
                    case "MIST": box.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Cylinder cylinder) {
                switch (theChoosenOne) {
                    case "NONE": cylinder.material = Material.NONE; break;
                    case "METAL": cylinder.material = Material.METAL; break;
                    case "DIELECTRIC": cylinder.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": cylinder.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": cylinder.material = Material.GLOSSY; break;
                    case "PLASTIC": cylinder.material = Material.PLASTIC; break;
                    case "MATTE": cylinder.material = Material.MATTE; break;
                    case "MIRROR": cylinder.material = Material.MIRROR; break;
                    case "TRANSLUCENT": cylinder.material = Material.TRANSLUCENT; break;
                    case "CHROME": cylinder.material = Material.CHROME; break;
                    case "MAGIC_GOO": cylinder.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": cylinder.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": cylinder.material = Material.CRYSTAL; break;
                    case "MIST": cylinder.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Prism p) {
                switch (theChoosenOne) {
                    case "NONE": p.material = Material.NONE; break;
                    case "METAL": p.material = Material.METAL; break;
                    case "DIELECTRIC": p.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": p.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": p.material = Material.GLOSSY; break;
                    case "PLASTIC": p.material = Material.PLASTIC; break;
                    case "MATTE": p.material = Material.MATTE; break;
                    case "MIRROR": p.material = Material.MIRROR; break;
                    case "TRANSLUCENT": p.material = Material.TRANSLUCENT; break;
                    case "CHROME": p.material = Material.CHROME; break;
                    case "MAGIC_GOO": p.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": p.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": p.material = Material.CRYSTAL; break;
                    case "MIST": p.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Cone t) {
                switch (theChoosenOne) {
                    case "NONE": t.material = Material.NONE; break;
                    case "METAL": t.material = Material.METAL; break;
                    case "DIELECTRIC": t.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": t.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": t.material = Material.GLOSSY; break;
                    case "PLASTIC": t.material = Material.PLASTIC; break;
                    case "MATTE": t.material = Material.MATTE; break;
                    case "MIRROR": t.material = Material.MIRROR; break;
                    case "TRANSLUCENT": t.material = Material.TRANSLUCENT; break;
                    case "CHROME": t.material = Material.CHROME; break;
                    case "MAGIC_GOO": t.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": t.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": t.material = Material.CRYSTAL; break;
                    case "MIST": t.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            } else if (s instanceof Octahedron o) {
                switch (theChoosenOne) {
                    case "NONE": o.material = Material.NONE; break;
                    case "METAL": o.material = Material.METAL; break;
                    case "DIELECTRIC": o.material = Material.DIELECTRIC; break;
                    case "LAMBERTIAN": o.material = Material.LAMBERTIAN; break;
                    case "GLOSSY": o.material = Material.GLOSSY; break;
                    case "PLASTIC": o.material = Material.PLASTIC; break;
                    case "MATTE": o.material = Material.MATTE; break;
                    case "MIRROR": o.material = Material.MIRROR; break;
                    case "TRANSLUCENT": o.material = Material.TRANSLUCENT; break;
                    case "CHROME": o.material = Material.CHROME; break;
                    case "MAGIC_GOO": o.material = Material.MAGIC_GOO; break;
                    case "ANODIZED_METAL": o.material = Material.ANODIZED_METAL; break;
                    case "CRYSTAL": o.material = Material.CRYSTAL; break;
                    case "MIST": o.material = Material.MIST; break;
                }

                editMaterialMenu.setSelectedItem(theChoosenOne);

            }


            drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);

        });

        editMaterial.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        editMaterial.add(editMaterialMenu);

        editMaterial.setVisible(true);
        return editMaterial;
    }

    JPanel editFuzz(JDialog editObjectWindow, Shape s) {
        JPanel editFuzz = new JPanel();
        editFuzz.setOpaque(false);
        editFuzz.setBackground(new java.awt.Color(0,0,0,0));
        editFuzz.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editFuzz.setLocation(editFuzz.getWidth() / 2 , editFuzz.getHeight() * 5);
        editFuzz.setBorder(BorderFactory.createEmptyBorder(0, ( getWidth() / 80 ) , 0, 0));

        editFuzz.setLayout(new BoxLayout(editFuzz, BoxLayout.X_AXIS));;

        JTextField editFuzzTextField = new JTextField();
        editFuzzTextField.setBackground(new java.awt.Color(0,0,0,0));
        editFuzzTextField.setForeground(java.awt.Color.white);

        if(s instanceof Sphere sphere){
            editFuzzTextField.setText(String.valueOf(sphere.fuzz));
        } else if(s instanceof Triangle triangle) {
            editFuzzTextField.setText(String.valueOf(triangle.fuzz));
        } else if(s instanceof Box box) {
            editFuzzTextField.setText(String.valueOf(box.fuzz));
        }
        else if(s instanceof Cylinder cylinder) {
            editFuzzTextField.setText(String.valueOf(cylinder.fuzz));
        }
        else if(s instanceof Prism p) {
            editFuzzTextField.setText(String.valueOf(p.fuzz));
        }
        else if(s instanceof Cone t) {
            editFuzzTextField.setText(String.valueOf(t.fuzz));
        }
        else if(s instanceof Octahedron o) {
            editFuzzTextField.setText(String.valueOf(o.fuzz));
        }


        editFuzzTextField.getDocument().addDocumentListener(new DocumentListener( ) {
            public void update(){
                String r = editFuzzTextField.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);
                    if(val < 0){ return; }

                    if(s instanceof Sphere sphere){
                        sphere.fuzz = val;
                    } else if(s instanceof Triangle triangle) {
                        triangle.fuzz = val;
                    } else if(s instanceof Box box) {
                        box.fuzz = val;
                    }
                    else if(s instanceof Cylinder cylinder) {
                        cylinder.fuzz = val;
                    }
                    else if(s instanceof Prism p) {
                        p.fuzz = val;
                    }
                    else if(s instanceof Cone t) {
                        t.fuzz = val;
                    }
                    else if(s instanceof Octahedron o) {
                        o.fuzz = val;
                    }


                    drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);

                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        editFuzz.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        editFuzz.add(editFuzzTextField);

        editFuzz.setVisible(true);
        return editFuzz;
    }

    JPanel editZPosition(JDialog editObjectWindow, Shape s) {
        JPanel editZPosition = new JPanel();
        editZPosition.setOpaque(false);
        editZPosition.setBackground(new java.awt.Color(0,0,0,0));
        editZPosition.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editZPosition.setLocation(editObjectWindow.getWidth() - ( editZPosition.getWidth() ) , editZPosition.getHeight() * 5);
        editZPosition.setBorder(BorderFactory.createEmptyBorder(0, 0 , 0, ( getWidth() / 100 )));

        editZPosition.setLayout(new BoxLayout(editZPosition, BoxLayout.X_AXIS));

        JLabel editZPositionText = new JLabel("Z Axis ");
        editZPositionText.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        editZPositionText.setForeground(java.awt.Color.white);

        JTextField editZPositionTextField = new JTextField();
        editZPositionTextField.setBackground(new java.awt.Color(0,0,0,0));
        editZPositionTextField.setForeground(java.awt.Color.white);

        if(s instanceof Sphere sphere){
            editZPositionTextField.setText(String.valueOf(sphere.center.z));
        } else if(s instanceof Triangle triangle){
            editZPositionTextField.setText(String.valueOf(triangle.getCentroidZ()));
        } else if(s instanceof Box box) {
            editZPositionTextField.setText(String.valueOf(box.getCenter().z));
        } else if(s instanceof Cylinder cylinder) {
            editZPositionTextField.setText(String.valueOf(cylinder.center.z));
        } else if(s instanceof Prism p) {
            editZPositionTextField.setText(String.valueOf(p.getCenter().z));
        } else if(s instanceof Cone t) {
            editZPositionTextField.setText(String.valueOf(t.getCenter().z));
        } else if(s instanceof Octahedron o) {
            editZPositionTextField.setText(String.valueOf(o.getCenter().z));
        }

        editZPositionTextField.getDocument().addDocumentListener(new DocumentListener( ) {
            public void update(){
                String r = editZPositionTextField.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);

                    if(s instanceof Sphere sphere){
                        sphere.center.z = val;
                    } else if(s instanceof Triangle triangle) {
                        triangle.moveZ(val);
                    } else if(s instanceof Box box){
                        box.moveZ(val);
                    } else if(s instanceof Cylinder cylinder){
                        cylinder.moveZ(val);
                    } else if(s instanceof Prism p){
                        p.moveZ(val);
                    } else if(s instanceof Cone t){
                        t.moveZ(val);
                    } else if(s instanceof Octahedron o){
                        o.moveZ(val);
                    }

                    drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        editZPosition.add(editZPositionText);
        editZPosition.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        editZPosition.add(editZPositionTextField);

        editZPositionTextField.setVisible(true);
        return editZPosition;
    }

    JPanel editYPosition(JDialog editObjectWindow, Shape s) {
        JPanel editYPosition = new JPanel();
        editYPosition.setOpaque(false);
        editYPosition.setBackground(new java.awt.Color(0,0,0,0));
        editYPosition.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editYPosition.setLocation(editObjectWindow.getWidth() - ( editYPosition.getWidth() ) , editYPosition.getHeight() * 4);
        editYPosition.setBorder(BorderFactory.createEmptyBorder(0, 0 , 0, ( getWidth() / 100 )));

        editYPosition.setLayout(new BoxLayout(editYPosition, BoxLayout.X_AXIS));

        JLabel editYPositionText = new JLabel("Y Axis ");
        editYPositionText.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        editYPositionText.setForeground(java.awt.Color.white);

        JTextField editYPositionTextField = new JTextField();
        editYPositionTextField.setBackground(new java.awt.Color(0,0,0,0));
        editYPositionTextField.setForeground(java.awt.Color.white);

        if(s instanceof Sphere sphere){
            editYPositionTextField.setText(String.valueOf(sphere.center.y));
        } else if(s instanceof Triangle triangle){
            editYPositionTextField.setText(String.valueOf(triangle.getCentroidY()));
        } else if(s instanceof Box box) {
            editYPositionTextField.setText(String.valueOf(box.getCenter().y));
        } else if(s instanceof Cylinder cylinder) {
            editYPositionTextField.setText(String.valueOf(cylinder.center.y));
        } else if(s instanceof Prism p) {
            editYPositionTextField.setText(String.valueOf(p.getCenter().y));
        } else if(s instanceof Cone t) {
            editYPositionTextField.setText(String.valueOf(t.getCenter().y));
        } else if(s instanceof Octahedron o) {
            editYPositionTextField.setText(String.valueOf(o.getCenter().y));
        }

        editYPositionTextField.getDocument().addDocumentListener(new DocumentListener( ) {
            public void update(){
                String r = editYPositionTextField.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);

                    if(s instanceof Sphere sphere){
                        sphere.center.y = val;
                    } else if(s instanceof Triangle triangle) {
                        triangle.moveY(val);
                    } else if(s instanceof Box box){
                        box.moveY(val);
                    } else if(s instanceof Cylinder cylinder){
                        cylinder.moveY(val);
                    } else if(s instanceof Prism p){
                        p.moveY(val);
                    } else if(s instanceof Cone t){
                        t.moveY(val);
                    } else if(s instanceof Octahedron o){
                        o.moveY(val);
                    }

                    drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);

                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        editYPosition.add(editYPositionText);
        editYPosition.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        editYPosition.add(editYPositionTextField);

        editYPositionTextField.setVisible(true);
        return editYPosition;
    }

    JPanel editXPosition(JDialog editObjectWindow, Shape s) {
        JPanel editXPosition = new JPanel();
        editXPosition.setOpaque(false);
        editXPosition.setBackground(new java.awt.Color(0,0,0,0));
        editXPosition.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editXPosition.setLocation(editObjectWindow.getWidth() - ( editXPosition.getWidth() ) , editXPosition.getHeight() * 3);
        editXPosition.setBorder(BorderFactory.createEmptyBorder(0, 0 , 0, ( getWidth() / 100 )));

        editXPosition.setLayout(new BoxLayout(editXPosition, BoxLayout.X_AXIS));

        JLabel editXPositionText = new JLabel("X Axis ");
        editXPositionText.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        editXPositionText.setForeground(java.awt.Color.white);

        JTextField editXPositionTextField = new JTextField();
        editXPositionTextField.setBackground(new java.awt.Color(0,0,0,0));
        editXPositionTextField.setForeground(java.awt.Color.white);

        if(s instanceof Sphere sphere){
            editXPositionTextField.setText(String.valueOf(sphere.center.x));
        } else if(s instanceof Triangle triangle){
            editXPositionTextField.setText(String.valueOf(triangle.getCentroidX()));
        } else if(s instanceof Box box) {
            editXPositionTextField.setText(String.valueOf(box.getCenter().x));
        } else if(s instanceof Cylinder cylinder) {
            editXPositionTextField.setText(String.valueOf(cylinder.center.x));
        } else if(s instanceof Prism p) {
            editXPositionTextField.setText(String.valueOf(p.getCenter().x));
        } else if(s instanceof Cone t) {
            editXPositionTextField.setText(String.valueOf(t.getCenter().x));
        } else if(s instanceof Octahedron o) {
            editXPositionTextField.setText(String.valueOf(o.getCenter().x));
        }

        editXPositionTextField.getDocument().addDocumentListener(new DocumentListener( ) {
            public void update(){
                String r = editXPositionTextField.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);

                    if(s instanceof Sphere sphere){
                        sphere.center.x = val;
                    } else if(s instanceof Triangle triangle) {
                        triangle.moveX(val);
                    } else if(s instanceof Box box){
                        box.moveX(val);
                    } else if(s instanceof Cylinder cylinder){
                        cylinder.moveX(val);
                    } else if(s instanceof Prism p){
                        p.moveX(val);
                    } else if(s instanceof Cone t){
                        t.moveX(val);
                    } else if(s instanceof Octahedron o){
                        o.moveX(val);
                    }

                    drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        editXPosition.add(editXPositionText);
        editXPosition.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        editXPosition.add(editXPositionTextField);

        editXPositionTextField.setVisible(true);
        return editXPosition;
    }

    JPanel editRadius(JDialog editObjectWindow, Shape s) {

        JPanel editRadius = new JPanel();
        editRadius.setOpaque(false);
        editRadius.setBackground(new java.awt.Color(0,0,0,0));
        editRadius.setSize(editObjectWindow.getWidth() / 3, editObjectWindow.getHeight() / 8);
        editRadius.setLocation(editObjectWindow.getWidth() - ( editRadius.getWidth() ) , editRadius.getHeight() * 2);
        editRadius.setBorder(BorderFactory.createEmptyBorder(0, 0 , 0, ( getWidth() / 100 )));
        editRadius.setLayout(new BoxLayout(editRadius, BoxLayout.X_AXIS));

        JLabel editRadiusText = new JLabel("Radius ");
        editRadiusText.setFont(new Font(Font.SERIF, Font.PLAIN, getWidth() / 100));
        editRadiusText.setForeground(java.awt.Color.white);

        JTextField editRadiusTextField = new JTextField();
        editRadiusTextField.setBackground(new java.awt.Color(0,0,0,0));
        editRadiusTextField.setForeground(java.awt.Color.white);

        if(s instanceof Sphere sphere) {
            editRadiusTextField.setText(String.valueOf(sphere.radius));
        } else if(s instanceof Triangle triangle){
            editRadiusTextField.setText(String.valueOf(triangle.getRadius()));
        } if(s instanceof Box box) {
            editRadiusTextField.setText(String.valueOf(box.getBoundingRadius()));
        } else if(s instanceof Cylinder cylinder){
            editRadiusTextField.setText(String.valueOf(cylinder.getRadius()));
        } else if(s instanceof Prism p){
            editRadiusTextField.setText(String.valueOf(p.getRadius()));
        } else if(s instanceof Cone t){
            editRadiusTextField.setText(String.valueOf(t.getRadius()));
        } else if(s instanceof Octahedron o){
            editRadiusTextField.setText(String.valueOf(o.getRadius()));
        }

            editRadiusTextField.getDocument().addDocumentListener(new DocumentListener( ) {
                public void update(){
                    String r = editRadiusTextField.getText().trim();
                    if(r.isEmpty()){ return; }

                    try{
                        double rad = Double.parseDouble(r);
                        if(rad <= 0){ return; }

                        if(s instanceof Sphere sphere) {
                            sphere.radius = rad;
                        } else if(s instanceof Triangle triangle){
                            triangle.setRadius(rad);
                        } if(s instanceof Box box) {
                            box.setRadius(rad);
                        } else if(s instanceof Cylinder cylinder){
                            cylinder.setRadius(rad);
                        } else if(s instanceof Prism p){
                            p.setRadius(rad);
                        } else if(s instanceof Cone c){
                            c.setRadius(rad);
                            c.setHeight(rad * Math.sqrt(3));
                        } else if(s instanceof Octahedron o){
                            o.setRadius(rad);
                        }


                        drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                    } catch(Exception e){};

                }

                public void insertUpdate(DocumentEvent e) { update(); }
                public void removeUpdate(DocumentEvent e) { update(); }
                public void changedUpdate(DocumentEvent e){ update(); }

            });

            editRadius.add(editRadiusText);
            editRadius.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
            editRadius.add(editRadiusTextField);

        editRadius.setVisible(true);
        return editRadius;
    }

    void MouseKeyWindow() {

        mouseKeyWindow = new JWindow();

        mouseKeyWindow.setBackground(new java.awt.Color(0,0,0,0));
        mouseKeyWindow.setSize(getWidth() / 4, getHeight() / 5);
        mouseKeyWindow.setLocation(
                getWidth() - mouseKeyWindow.getWidth(),
                getHeight() - mouseKeyWindow.getHeight()
        );
        mouseKeyWindow.setAlwaysOnTop(true);
        mouseKeyWindow.setLayout(null);

        mouseKeyWindow.add(mousePanel(mouseKeyWindow));
        mouseKeyWindow.add(keyPanel(mouseKeyWindow));
        mouseKeyWindow.setVisible(true);

    }

    JPanel keyPanel(JWindow mouseKeyWindow) {
        JPanel keyPanel = new JPanel();
        keyPanel.setBackground(new java.awt.Color(50,0,0,0));
        keyPanel.setSize(mouseKeyWindow.getWidth() / 2, mouseKeyWindow.getHeight());
        keyPanel.setLocation(mouseKeyWindow.getWidth() / 2, 0);

        JComponent keyImageComponent = new JComponent( ) {
            protected void paintComponent(Graphics g) {
                g.drawImage(ArrowsImage, 0, 0, this.getWidth(), this.getHeight() / 2, this);
                g.drawImage(XbuttonImage, this.getWidth() / 2 - this.getWidth() / 3, this.getHeight() / 2 + this.getHeight() / 8, this.getWidth() / 3, this.getHeight() / 3, this);
                g.drawImage(ZbuttonImage, this.getWidth() / 2, this.getHeight() / 2 + this.getHeight() / 8, this.getWidth() / 3, this.getHeight() / 3, this);
            }
        };
        keyImageComponent.setPreferredSize(new Dimension(keyPanel.getWidth(), keyPanel.getHeight() / 2 + keyPanel.getHeight() / 4));

        JLabel keyText = new JLabel("Press keys to move around");
        keyText.setFont(new Font(Font.SERIF, Font.ITALIC, getWidth() / 100));
        keyText.setSize(keyPanel.getWidth(), keyPanel.getHeight() / 4);
        keyText.setForeground(java.awt.Color.white);

        keyPanel.add(keyImageComponent);
        keyPanel.add(keyText);
        keyPanel.setVisible(true);
        return keyPanel;
    }

    JPanel mousePanel(JWindow mouseKeyWindow) {
        JPanel mousePanel = new JPanel();
        mousePanel.setBackground(new java.awt.Color(0,0,0,0));
        mousePanel.setSize(mouseKeyWindow.getWidth() / 2, mouseKeyWindow.getHeight());
        mousePanel.setLocation(0,0);

        JComponent mouseImageComponent = new JComponent() {
            protected void paintComponent(Graphics g) {
                g.drawImage(MouseImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };
        mouseImageComponent.setPreferredSize(new Dimension(mousePanel.getWidth(),mousePanel.getHeight() / 2 + mousePanel.getHeight() / 4));

        JLabel mouseText = new JLabel("Drag mouse to view around");
        mouseText.setForeground(java.awt.Color.white);
        mouseText.setFont(new Font(Font.SERIF, Font.ITALIC, getWidth() / 100));
        mouseText.setSize(mousePanel.getWidth(), mousePanel.getHeight() / 4);

        mousePanel.add(mouseImageComponent);
        mousePanel.add(mouseText);
        mousePanel.setVisible(true);
        return mousePanel;
    }

    void addOverlayColor() {
        LabelX.setForeground(java.awt.Color.white);
        LabelY.setForeground(java.awt.Color.white);
        LabelZ.setForeground(java.awt.Color.white);
        LabelChangerX.setForeground(java.awt.Color.white);
        LabelChangerY.setForeground(java.awt.Color.white);
        LabelChangerZ.setForeground(java.awt.Color.white);
    }

    void addOverlayFonts() {
        LabelFont = new Font(Font.SERIF, Font.PLAIN, getWidth() / 50);
        UpdateFont = new Font(Font.SANS_SERIF, Font.PLAIN, getWidth() / 50);

        LabelX.setFont(LabelFont);
        LabelY.setFont(LabelFont);
        LabelZ.setFont(LabelFont);
        LabelChangerX.setFont(UpdateFont);
        LabelChangerY.setFont(UpdateFont);
        LabelChangerZ.setFont(UpdateFont);
    }

    void axisWindow() {

        axis = new JDialog();
        axis.setUndecorated(true);
        axis.setBackground(new java.awt.Color(0, 0, 0, 0));
        axis.setSize(getWidth() / 10, getHeight() / 8);
        axis.setLayout(new GridLayout(3,2));
        axis.setAlwaysOnTop(true);

        axis.add(LabelX); axis.add(LabelChangerX);
        axis.add(LabelY); axis.add(LabelChangerY);
        axis.add(LabelZ); axis.add(LabelChangerZ);

        axis.setLocation(
                (int) getLocation().getX() +  ( getWidth() / 32 ) ,
                (int) getLocation().getY() + ( getHeight() / 2 + getHeight() / 3 )
        );

        axis.setFocusable(false);
        axis.setVisible(true);
    }

    void Components() {
        LabelX = new JLabel("X");
        LabelY = new JLabel("Y");
        LabelZ = new JLabel("Z");

        LabelChangerX.setBorder(BorderFactory.createEmptyBorder( 0,0,0,0 ));
        LabelChangerX.setBackground(new java.awt.Color(0,0,0,0));
        LabelChangerX.setForeground(java.awt.Color.white);
        LabelChangerX.setFocusable(false);
        LabelChangerX.addMouseListener(new MouseAdapter( ) {
            public void mouseEntered(MouseEvent e) {
                LabelChangerX.setFocusable(true);
            }
            public void mouseExited(MouseEvent e){
                LabelChangerX.setFocusable(false);
                Window.this.requestFocus();
            }
        });
        LabelChangerX.getDocument().addDocumentListener(new DocumentListener( ){
            public void update(){
                String r = LabelChangerX.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);
                    if (LabelChangerX.hasFocus()){
                        M_X = val;
                        drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                    }
                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e){ update(); }
        });

        LabelChangerY.setBorder(BorderFactory.createEmptyBorder( 0,0,0,0 ));
        LabelChangerY.setBackground(new java.awt.Color(0,0,0,0));
        LabelChangerY.setForeground(java.awt.Color.white);
        LabelChangerY.setFocusable(false);
        LabelChangerY.addMouseListener(new MouseAdapter( ) {
            public void mouseEntered(MouseEvent e) {
                LabelChangerY.setFocusable(true);
            }
            public void mouseExited(MouseEvent e){
                LabelChangerY.setFocusable(false);
                Window.this.requestFocus();
            }
        });
        LabelChangerY.getDocument().addDocumentListener(new DocumentListener( ){
            public void update(){
                String r = LabelChangerY.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);
                    if (LabelChangerY.hasFocus()){
                        M_Y = val;
                        drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                    }
                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e){ update(); }
        });

        LabelChangerZ.setBackground(new java.awt.Color(0,0,0,0));
        LabelChangerZ.setForeground(java.awt.Color.white);
        LabelChangerZ.setBorder(BorderFactory.createEmptyBorder( 0,0,0,0 ));
        LabelChangerZ.setFocusable(false);
        LabelChangerZ.addMouseListener(new MouseAdapter( ) {
            public void mouseEntered(MouseEvent e) {
                LabelChangerZ.setFocusable(true);
            }
            public void mouseExited(MouseEvent e){
                LabelChangerZ.setFocusable(false);
                Window.this.requestFocus();
            }
        });
        LabelChangerZ.getDocument().addDocumentListener(new DocumentListener( ){
            public void update(){
                String r = LabelChangerZ.getText().trim();
                if(r.isEmpty()){ return; }

                try{
                    double val = Double.parseDouble(r);
                    if (LabelChangerZ.hasFocus()){
                        M_Z = val;
                        drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
                    }
                } catch(Exception e){};

            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e){ update(); }
        });

        MouseImage = new ImageIcon(getClass().getResource("/Resources/MouseImage.png")).getImage();
        ArrowsImage = new ImageIcon(getClass().getResource("/Resources/ArrowsImage.png")).getImage();
        ZbuttonImage = new ImageIcon(getClass().getResource("/Resources/ZButtonImage.png")).getImage();
        XbuttonImage = new ImageIcon(getClass().getResource("/Resources/XButtonImage.png")).getImage();

        addOverlayFonts();
        addOverlayColor();
    }

    void window() {
        setBackground(java.awt.Color.BLACK);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setResizable(false);
        setVisible(true);
        setLayout(null);

        addKeyListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
    }

    void setup() throws IOException {

        SCREEN = Toolkit.getDefaultToolkit().getScreenSize(); setSize((int)(SCREEN.width),(int)(SCREEN.height));
        V_YAXIS = 2; V_XAXIS = 1; V_ZAXIS = -3; M_X = 0; M_Y = 0; M_Z = 0; latestX = 0; latestY = 0;
        HEIGHT = SCREEN.height; WIDTH = SCREEN.width; ANTI_ALISING_SAMPLES = 0;

        LabelChangerX = new JTextField(String.valueOf(M_X));
        LabelChangerY = new JTextField(String.valueOf(M_Y));
        LabelChangerZ = new JTextField(String.valueOf(M_Z));

        Point3D startpos = new Point3D(M_X - V_XAXIS, M_Y - V_YAXIS, M_Z - V_ZAXIS).normalize();
        yaw = Math.toDegrees(Math.atan2(startpos.x, startpos.z)); pitch = Math.toDegrees(Math.asin(startpos.y));
        radius = new Point3D(V_XAXIS - M_X, V_YAXIS - M_Y, V_ZAXIS - M_Z).length();

        go = 0; drag = false; random = new Random(); objectCounter = 0;
        keyClock = Executors.newSingleThreadScheduledExecutor();
        mouseClock = Executors.newSingleThreadScheduledExecutor();
        shapeButtonMap = new HashMap<>();

        String[] sceneNames = {
                "PureSky", "Room", "MirrorHall", "ModernHall", "ChristmasHall",
                "Lounge", "Garden", "Backyard", "Lake", "Pool" };
        Random random = new Random();
        int index = random.nextInt(sceneNames.length);
        String selectedScene = sceneNames[index];

        selectedloadScene = new String[1];
        selectedloadScene[0] = selectedScene;

        try {
            environmentMap = ImageIO.read(
                    getClass().getResourceAsStream("/Resources/Scenes/" + selectedScene.toLowerCase() + ".jpg")
            );
        } catch(IOException e){ e.printStackTrace(); }


        WORLD = new ArrayList<>();

        Color silver = new Color(0.92f, 0.92f, 0.95f);
        double fuzzLow = 0.01; double fuzzHigh = 0.05; double groundY = -1.0;

        WORLD.add(new Sphere(
                new Point3D(0, 0.6, 0),
                1.2,
                silver,
                Material.CHROME,
                fuzzLow
        ));
        WORLD.add(new Octahedron(
                new Point3D(-2.5, 2.2, -1.5),
                0.6,
                silver,
                Material.ANODIZED_METAL,
                fuzzLow
        ));
        WORLD.add(new Octahedron(
                new Point3D(2.5, 2.2, -1.5),
                0.6,
                silver,
                Material.ANODIZED_METAL,
                fuzzLow
        ));
        for (int i = -2; i <= 2; i++) {
            WORLD.add(new Sphere(
                    new Point3D(i * 1.2, -0.3, -3),
                    0.3,
                    silver,
                    Material.DIELECTRIC,
                    0.02 + (i % 3) * 0.01
            ));
        }
        WORLD.add(new Box(
                new Point3D(-3.5, groundY, 0),
                1.2, 1.2, 1.2,
                silver,
                Material.MAGIC_GOO,
                fuzzLow
        ));
        WORLD.add(new Box(
                new Point3D(3.5, groundY, 0),
                1.2, 1.2, 1.2,
                silver,
                Material.MAGIC_GOO,
                fuzzLow
        ));
        WORLD.add(new Triangle(
                new Point3D(-2.5, 3.2, -1.5),
                new Point3D(0, 4.5, 0),
                new Point3D(2.5, 3.2, -1.5),
                silver,
                Material.CRYSTAL,
                fuzzLow
        ));
        WORLD.add(new Triangle(
                new Point3D(-1.5, groundY, -3.0),
                new Point3D(-0.5, 1.2, -3.0),
                new Point3D(-1.0, 0.7, -3.0),
                silver,
                Material.MIRROR,
                fuzzLow
        ));
        WORLD.add(new Triangle(
                new Point3D(1.5, groundY, -3.0),
                new Point3D(0.5, 1.2, -3.0),
                new Point3D(1.0, 0.7, -3.0),
                silver,
                Material.MIRROR,
                fuzzLow
        ));

        drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
    }

    void drawImage(int HEIGHT, int WIDTH, int ANTI_ALISING_SAMPLES) {

        newCameraPosition();

        BEINGRENDERED = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        final int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int sliceHeight = HEIGHT / numThreads;

        for (int i = 0; i < numThreads; i++) {
            final int startY = i * sliceHeight;
            final int endY = (i == numThreads - 1) ? HEIGHT : (i + 1) * sliceHeight;

            int finalWIDTH = WIDTH;
            int finalHEIGHT = HEIGHT;
            executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < finalWIDTH; x++) {
                        double u = (double) x / finalWIDTH;
                        double v = (double) (finalHEIGHT - y) / finalHEIGHT;
                        Ray ray = CAMERA.getRay(u, v);
                        Color pixelColor = new Color(Main.rayColor(WORLD, ray, environmentMap, 10));
                        synchronized (BEINGRENDERED) {
                            BEINGRENDERED.setRGB(x, y, pixelColor.colorToInteger());
                        }
                    }
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IMG = BEINGRENDERED;
        syncPositionUI();
        repaint();
    }

    void newCameraPosition() {
        double yawRad = Math.toRadians(yaw); double pitchRad = Math.toRadians(pitch);
        double dirX = Math.cos(pitchRad) * Math.sin(yawRad); double dirY = Math.sin(pitchRad); double dirZ = Math.cos(pitchRad) * Math.cos(yawRad);
        Point3D direction = new Point3D(dirX, dirY, dirZ).normalize();
        Point3D lookFrom = new Point3D(M_X, M_Y, M_Z).add(direction.mul(-radius));
        Point3D lookAt = new Point3D(M_X, M_Y, M_Z); Point3D vup = new Point3D(0, 1, 0);
        CAMERA = new Camera(lookFrom, lookAt, vup, 90, (double) WIDTH / HEIGHT);
    }

    public void paint(Graphics g){
        if(IMG == null) return;
        g.drawImage(
                IMG,
                ( getWidth() / 2 ) - ( WIDTH / 2),
                ( getHeight() / 2 ) - ( HEIGHT / 2 ),
                WIDTH,
                HEIGHT,
                this
        );
    }

    private void syncPositionUI() {
        if (LabelChangerX.hasFocus() || LabelChangerY.hasFocus() || LabelChangerZ.hasFocus()) return;
        SwingUtilities.invokeLater(() -> {
            LabelChangerX.setText(String.format("%.0f", M_X));
            LabelChangerY.setText(String.format("%.0f", M_Y));
            LabelChangerZ.setText(String.format("%.0f", M_Z));
        });
    }
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        double moveSpeed = 0.5; double yawRad = Math.toRadians(yaw); double pitchRad = Math.toRadians(pitch);
        Point3D forward = new Point3D(Math.cos(pitchRad) * Math.sin(yawRad), Math.sin(pitchRad),Math.cos(pitchRad) * Math.cos(yawRad)).normalize();
        Point3D right = forward.cross(new Point3D(0, 1, 0)).normalize(); Point3D up = right.cross(forward).normalize();
        Point3D movement = new Point3D(0, 0, 0);
        switch (e.getKeyCode()) {
            case 38: movement = forward.mul(moveSpeed);  break;     // AHEAD
            case 40: movement = forward.mul(-moveSpeed); break;     // BACKWARDS
            case 37: movement = right.mul(-moveSpeed);   break;     // LEFT
            case 39: movement = right.mul(moveSpeed);    break;     // RIGHT
            case 88: movement = up.mul(moveSpeed);       break;     // UP
            case 90: movement = up.mul(-moveSpeed);      break;     // DOWN
            default: return;
        }
        M_X += movement.x; M_Y += movement.y; M_Z += movement.z;
        drawImage(HEIGHT / 20, WIDTH / 20, ANTI_ALISING_SAMPLES);
        if(keySchedule != null && keySchedule.isDone() == false) keySchedule.cancel(true);
        keySchedule = keyClock.schedule(() -> drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES),250,TimeUnit.MILLISECONDS);
    }

    public void mouseDragged(MouseEvent e) {
        double beingDragged_X = e.getX(); double beingDragged_Y = e.getY();
        if (selectObjectMouseCheckbox.isSelected() == true) {
            if (selectedShape != null) {
                Ray dragRay = CAMERA.getRayFromScreen(e.getX(), e.getY(), WIDTH, HEIGHT);
                if (selectedShape instanceof Sphere sphere) {
                    double originalY = sphere.center.y;
                    Point3D newPos = Point3D.projectToGround(dragRay, originalY);
                    sphere.setCenter(newPos);
                } else if (selectedShape instanceof Triangle triangle) {
                    double avgY = (triangle.v0.y + triangle.v1.y + triangle.v2.y) / 3.0;
                    Point3D newCenter = Point3D.projectToGround(dragRay, avgY);
                    triangle.setCenter(newCenter);
                } else if (selectedShape instanceof Box box) {
                    Point3D oldCenter = box.getCenter();
                    Point3D newCenter = Point3D.projectToGround(dragRay, oldCenter.y);
                    Point3D translation = newCenter.sub(oldCenter);
                    box.setCenter(box.getCenter().add(translation));
                } else if (selectedShape instanceof Cylinder cylinder) {
                    double originalY = cylinder.center.y;
                    Point3D newBaseCenter = Point3D.projectToGround(dragRay, originalY);
                    cylinder.center = newBaseCenter;
                } else if (selectedShape instanceof Prism prism) {
                    double avgY = 0; for (Point3D v : prism.vertices) { avgY += v.y; } avgY /= prism.vertices.length;
                    Point3D newCenter = Point3D.projectToGround(dragRay, avgY);
                    double cx = 0, cy = 0, cz = 0; for (Point3D v : prism.vertices) { cx += v.x; cy += v.y; cz += v.z; }
                    cx /= prism.vertices.length; cy /= prism.vertices.length; cz /= prism.vertices.length;
                    Point3D centroid = new Point3D(cx, cy, cz); Point3D translation = newCenter.sub(centroid);
                    for (int i = 0; i < prism.vertices.length; i++) {
                        prism.vertices[i].x += translation.x;
                        prism.vertices[i].y += translation.y;
                        prism.vertices[i].z += translation.z;
                    }
                } else if (selectedShape instanceof Cone cone) {
                    double originalY = cone.getCenter().y;
                    Point3D newCenter = Point3D.projectToGround(dragRay, originalY);
                    cone.setCenter(newCenter);
                } else if (selectedShape instanceof Octahedron octahedron) {
                    double avgY = 0; for (Point3D v : octahedron.vertices) { avgY += v.y; } avgY /= octahedron.vertices.length;
                    Point3D newCenter = Point3D.projectToGround(dragRay, avgY);
                    double cx = 0, cy = 0, cz = 0; for (Point3D v : octahedron.vertices) { cx += v.x; cy += v.y; cz += v.z; }
                    cx /= octahedron.vertices.length; cy /= octahedron.vertices.length; cz /= octahedron.vertices.length;
                    Point3D centroid = new Point3D(cx, cy, cz);
                    Point3D translation = newCenter.sub(centroid);
                    for (int i = 0; i < octahedron.vertices.length; i++) {
                        octahedron.vertices[i].x += translation.x;
                        octahedron.vertices[i].y += translation.y;
                        octahedron.vertices[i].z += translation.z;
                    }
                }

                drawImage(HEIGHT / 20, WIDTH / 20, ANTI_ALISING_SAMPLES);

            }
        } else {
            yaw += - ( beingDragged_X - latestX ) * 0.1; pitch -= ( beingDragged_Y - latestY ) * 0.1;
            if (Math.abs(beingDragged_X - latestX) < 10 && Math.abs(beingDragged_Y - latestY) < 10) return;
            if (drag) { drawImage(HEIGHT / 20, WIDTH / 20, ANTI_ALISING_SAMPLES); }
            latestX = beingDragged_X; latestY = beingDragged_Y;
        }
    }
    public void mouseMoved(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e)  {
        latestX = e.getX(); latestY = e.getY(); drag = true;
        if (selectObjectMouseCheckbox.isSelected()) {
            drag = false;
            Ray clickRay = CAMERA.getRayFromScreen(e.getX(), e.getY(), WIDTH, HEIGHT);
            selectedShape = getClosestIntersectedShape(clickRay);
            JButton btn = shapeButtonMap.get(selectedShape);
            editObjectWindow(selectedShape, WORLD.indexOf(selectedShape), btn);
        }
    }
    public void mouseReleased(MouseEvent e) {
        drag = false; drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES);
    }
    public void mouseWheelMoved(MouseWheelEvent e) {
            double offset_Z = e.getPreciseWheelRotation() / 10; radius += offset_Z * 0.25;
            drawImage(HEIGHT / 20, WIDTH / 20, ANTI_ALISING_SAMPLES);
            if(mouseSchedule != null && mouseSchedule.isDone() == false) mouseSchedule.cancel(true);
            mouseSchedule = mouseClock.schedule(() -> drawImage(HEIGHT, WIDTH, ANTI_ALISING_SAMPLES),250,TimeUnit.MILLISECONDS);
    }

    public Shape getClosestIntersectedShape(Ray ray) {
        Shape closest = null;
        double closestT = Double.MAX_VALUE;
        for (Shape s : WORLD) {
            double t = -1;
            if (s instanceof Sphere sphere) {
                t = Main.intersectRaySphere(ray, sphere);
            } else if (s instanceof Triangle triangle) {
                t = Main.intersectRayTriangle(ray, triangle);
            } else if (s instanceof Box box) {
                t = Main.intersectRayBox(ray, box);
            } else if (s instanceof Cylinder cylinder) {
                t = Main.intersectRayCylinder(ray, cylinder);
            } else if (s instanceof Prism p) {
                t = Main.intersectRayPrism(ray, p);
            } else if (s instanceof Cone cone) {
                t = Main.intersectRayCone(ray, cone);
            } else if (s instanceof Octahedron o) {
                t = Main.intersectRayOctahedron(ray, o);
            }
            if (t > 0 && t < closestT) {
                closestT = t;
                closest = s;
            }
        }
        return closest;
    }

}
