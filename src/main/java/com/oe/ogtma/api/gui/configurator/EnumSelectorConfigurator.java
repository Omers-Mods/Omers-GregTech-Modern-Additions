package com.oe.ogtma.api.gui.configurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Accessors(chain = true)
public class EnumSelectorConfigurator<T extends Enum<T> & EnumSelectorWidget.SelectableEnum> implements IFancyConfiguratorButton {
    protected Component title;
    protected EnumSelectorWidget<T> widget;
    @Getter
    @Setter
    protected T selected;
    
    public EnumSelectorConfigurator(T initial, T[] values, Consumer<T> onChanged) {
        selected = initial;
        var consumer = onChanged.andThen(this::setSelected);
        widget = new EnumSelectorWidget<>(0, 0, 18, 18, values, initial, consumer);
    }


    public EnumSelectorConfigurator(Component title, T initial, T[] values, Consumer<T> onChanged) {
        this(initial, values, onChanged);
        this.title = title;
    }

    @Override
    public Component getTitle() {
        if (title == null) {
            var name = selected.getClass().getSimpleName();
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            var elements = Arrays.stream(name.split("(?<=[a-z])(?=[A-Z])"))
                    .map(String::toLowerCase).toArray(CharSequence[]::new);
            title = Component.translatable("ogtma.option." + String.join("_", elements));
        }
        return title;
    }

    @Override
    public IGuiTexture getIcon() {
        return selected.getIcon();
    }

    @Override
    public List<Component> getTooltips() {
        return List.of(getTitle(), Component.translatable(selected.getTooltip()));
    }

    @Override
    public void onClick(ClickData clickData) {
        widget.mouseClicked((double) widget.getSizeWidth() / 2, (double) widget.getSizeHeight() / 2, clickData.button);
    }

    @Override
    public Widget createConfigurator() {
        return widget;
    }
}
