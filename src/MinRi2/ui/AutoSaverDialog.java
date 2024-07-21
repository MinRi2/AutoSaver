package MinRi2.ui;

import MinRi2.*;
import MinRi2.AutoSaver.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/7/19
 */
public class AutoSaverDialog extends BaseDialog{
    public static final int maxSavesAmount = 35;
    public static final float maxSavePerMinute = 20;

    private static final Seq<SaveMeta> tempMetas = new Seq<>();

    private ButtonStyle saveManuallyButtonStyle, saveButtonStyle;
    private LabelStyle indexStyle, titleStyle;

    private Table savesTable;

    private float saveWidth;

    private boolean isSetup;

    public AutoSaverDialog(){
        super("@auto-saver");

        shown(() -> {
            if(!isSetup){
                setup();
            }

            rebuildCont();
        });
    }

    private void setup(){
        saveManuallyButtonStyle = new ButtonStyle(){{
            up = getColoredRegion(Pal.lightishGray);
            down = getColoredRegion(Pal.accent);
            over = getColoredRegion(Pal.gray);
            disabled = getColoredRegion(Pal.darkestGray);
        }};

        saveButtonStyle = new ButtonStyle(){{
            up = getColoredRegion(Pal.gray);
            down = getColoredRegion(Pal.accent);
            over = getColoredRegion(Pal.lightishGray);
        }};

        titleStyle = new LabelStyle(Styles.outlineLabel){{
           fontColor = Pal.accent;
        }};

        indexStyle = new LabelStyle(Styles.outlineLabel){{
            fontColor = Pal.lightishGray;
        }};

        savesTable = new Table();

        Events.on(AutoSaveEvent.class, e -> {
            rebuildSavesTable();
        });

        rebuildCont();
        resized(this::rebuildCont);

        addCloseButton();

        isSetup = true;
    }

    private void rebuildCont(){
        cont.clearChildren();

        rebuildSavesTable();

        saveWidth = Core.graphics.getWidth() * Scl.scl(Core.graphics.getWidth() > 1000 ? 0.5f : 0.8f);

        cont.defaults().pad(0);

        cont.image().color(Pal.darkerGray).width(8).growY();

        cont.table(right -> {
            divide(right, "Settings");

            right.table(getColoredRegion(Pal.lightishGray), this::setupSettingsTable).padTop(4).padBottom(16).growX();

            divide(right, "Saves");

            right.pane(Styles.noBarPane, table -> {
                table.background(Styles.grayPanel);

                table.add(savesTable).grow();
            }).padTop(4).grow();
        }).width(saveWidth).growY();
    }

    private void rebuildSavesTable(){
        tempMetas.set(SaverVars.saver.getMetas());
        Seq<SaveMeta> metas = tempMetas.reverse();

        savesTable.clearChildren();

        savesTable.top();
        savesTable.defaults().top();

        savesTable.button(b -> {
            b.image(Icon.save).size(64).pad(12);
            b.add(Core.bundle.get(SaverVars.modPrefix + "saveManually"));
        }, saveManuallyButtonStyle, () -> {
            SaverVars.saver.saveData();
        }).padTop(8f).padBottom(8f).height(saveWidth / 6f).growX().disabled(b -> SaverVars.saver.saving);

        savesTable.row();

        if(metas.isEmpty()){
            savesTable.add(Core.bundle.get(SaverVars.modPrefix + "noSaves"));
            return;
        }

        int index = 1;
        for(SaveMeta meta : metas){
            int finalIndex = index;
            savesTable.button(t -> {
                t.table(indexTable -> {
                    indexTable.add("" + finalIndex).style(indexStyle).pad(8);
                    indexTable.row();
                    indexTable.image().color(Pal.darkerGray).size(32, 8);
                }).expandY();

                setupSaveMetaTable(t, meta);
            }, saveButtonStyle, () -> {
                if(Vars.state.isGame()){
                    Vars.ui.showInfo(Core.bundle.get(SaverVars.modPrefix + "getBackMenu"));
                    return;
                }

                Vars.ui.showConfirm(Core.bundle.format(SaverVars.modPrefix + "readData", meta.saveDate), () -> {
                    SaverVars.saver.readData(meta);
                });
            }).padTop(8f).padBottom(8f).height(saveWidth / 3f).growX();

            savesTable.row();
            index++;
        }
    }

    private void setupSettingsTable(Table settingsTable){
        settingsTable.defaults().pad(8).growX();

        booleanSetting(settingsTable, "autoSave");
        booleanSetting(settingsTable, "saveMods");

        numberSetting(settingsTable, "savesAmount", 1, maxSavesAmount, 1, Number::intValue);
        numberSetting(settingsTable, "savePerMinute", 1, maxSavePerMinute, 1, Number::floatValue, n -> n + "min");
    }

    private void setupSaveMetaTable(Table table, SaveMeta meta){
        table.table(Styles.grayPanel, rightTable -> {
            rightTable.top();

            rightTable.table(infoTable -> {
                infoTable.defaults().pad(8f).left().top();

                addInfo(infoTable, "date", meta.saveDate);
                addInfo(infoTable, "customMaps", meta.customMaps);
                infoTable.row();
                addInfo(infoTable, "saves", meta.saves);
                addInfo(infoTable, "schematics", meta.schematics);
                infoTable.row();
                addInfo(infoTable, "mods", meta.mods);
            }).pad(8f).grow();

            rightTable.defaults().reset();

            rightTable.image().color(Pal.darkerGray).width(8).growY();
            rightTable.row();
            rightTable.image().color(Pal.darkerGray).height(8).colspan(rightTable.getColumns()).growX();
        }).pad(24f).padLeft(8f).grow();
    }

    private void divide(Table table, String title){
        if(table.getCells().size > 0 && !table.getCells().peek().isEndRow()){
            table.row();
        }

        table.table(t -> {
            t.image().color(Pal.darkerGray).size(32, 8).top();
            t.add(title).width(128f).style(titleStyle).labelAlign(Align.center);
            t.image().color(Pal.darkerGray).height(8).growX().top();
        }).growX();
        table.row();
    }

    private void addInfo(Table table, String name, Object value){
        table.table(content -> {
            content.add(Core.bundle.get(SaverVars.modPrefix + name)).color(Pal.lightishGray);
            content.add(""  + value).style(Styles.outlineLabel).padLeft(8).expand().left();
        }).padLeft(4f).padTop(8f).growX();
    }

    private static Drawable getColoredRegion(Color color){
        return ((TextureRegionDrawable)Tex.whiteui).tint(color);
    }

    private static void booleanSetting(Table table, String name){
        BorderColorImage image = new BorderColorImage(SaverVars.setting.get(name) ? Color.green : Color.red);

        table.button(b -> {
            b.add(image).size(32);
            b.add(Core.bundle.get(SaverVars.settingPrefix + name, name)).style(Styles.outlineLabel).padLeft(8).expandX().left();
        }, Styles.clearNonei, () -> {
            boolean old = SaverVars.setting.get(name);
            SaverVars.setting.put(name, !old);
        }).row();

        SaverVars.setting.<Boolean>watch(name, value -> {
            image.setColorSmooth(value ? Color.green : Color.red);
        });
    }

    private static void numberSetting(Table table,
                                      String name,
                                      float min,
                                      float max,
                                      float step,
                                      Func<Number, ? extends Number> caster){
        numberSetting(table, name, min, max, step, caster, n -> "" + n);
    }

    private static void numberSetting(Table table,
                                      String name,
                                      float min,
                                      float max,
                                      float step,
                                      Func<Number, ? extends Number> caster,
                                      Func<Number, String> formatter){
        table.table(t -> {
            Number value = SaverVars.setting.get(name);

            Slider slider = new Slider(min, max, step, false);
            slider.setValue(value.floatValue());
            slider.moved(n -> SaverVars.setting.put(name, caster.get(n)));

            t.stack(slider, new Table(content -> {
                content.touchable = Touchable.disabled;

                content.add(Core.bundle.get(SaverVars.settingPrefix + name, name), Styles.outlineLabel).left().growX().wrap();
                Label label = content.add(formatter.get(SaverVars.setting.get(name))).padRight(8).right().get();

                SaverVars.setting.<Number>watch(name, n -> {
                    label.setText(formatter.get(n));
                });
            })).width(300).expandX().left();
        }).row();
    }
}
