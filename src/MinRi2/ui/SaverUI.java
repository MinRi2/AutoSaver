package MinRi2.ui;

import MinRi2.*;
import arc.*;
import arc.flabel.*;
import arc.func.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;

/**
 * @author minri2
 * Create by 2024/7/21
 */
public class SaverUI{
    public static Runnable showSaving(){
        Table table = new Table();

        table.touchable = Touchable.disabled;

        Image image = table.image(Tex.alphaaaa).get();

        table.row();
        table.add(Core.bundle.get(SaverVars.modPrefix + "saving", "Auto Saving")).style(Styles.outlineLabel);
        table.add(new FLabel("{WIND}..."));

        Timekeeper timekeeper = new Timekeeper(1.25f);
        timekeeper.reset();

        table.update(() -> {
            table.toFront();

            if(timekeeper.get()){
                timekeeper.reset();

                image.setOrigin(Align.center);
                image.actions(Actions.rotateBy(-360, 1f, Interp.smooth));
            }
        });

        Core.scene.root.fill(t -> {
            t.bottom();

            t.add(table);
        });

        return () -> {
            table.actions(
            Actions.delay(5), // 看看alpha动画吧!
            Actions.scaleTo(0, 0),
            Actions.remove()
            );
        };
    }
}
