package org.linn.woecrit.client.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.linn.woecrit.client.WoecritClient;
import org.linn.woecrit.client.freecam.Freecam;
import org.linn.woecrit.client.render.GhostRender;

public class WoecritMainScreen extends Screen {
    private TexturedButtonWidget freecamButton;
    private TexturedButtonWidget renderButton;

    public WoecritMainScreen() {
        super(Text.translatable("woecrit_screen.main.title"));
    }

    @Override
    protected void init() {
        this.freecamButton = new TexturedButtonWidget(0, 0, 20, 20,
                new ButtonTextures(
                        Identifier.of("woecrit", "freecam_button"),
                        Identifier.of("woecrit", "freecam_button_disabled"),
                        Identifier.of("woecrit", "freecam_button_highlighted")
                ),
                btn -> {
                    client.player.sendMessage(Text.literal("Toggle!"), true);
                    Freecam.toggle();
                    this.freecamButton.setTooltip(Tooltip.of(
                            Freecam.isEnabled() ?
                                    Text.translatable(
                                            "options.on.composed",
                                            Text.translatable("woecrit_screen.freecam.tooltip")
                                    ) :
                                    Text.translatable(
                                            "options.off.composed",
                                            Text.translatable("woecrit_screen.freecam.tooltip")
                                    )
                    ));
                }
        );
        this.freecamButton.setTooltip(Tooltip.of(
                Freecam.isEnabled() ?
                        Text.translatable(
                                "options.on.composed",
                                Text.translatable("woecrit_screen.freecam.tooltip")
                        ) :
                        Text.translatable(
                                "options.off.composed",
                                Text.translatable("woecrit_screen.freecam.tooltip")
                        )
        ));
        this.addDrawableChild(this.freecamButton);


        this.renderButton = new TexturedButtonWidget(0, 20, 20, 20,
                new ButtonTextures(
                        Identifier.of("woecrit", "renderer_button"),
                        Identifier.of("woecrit", "renderer_button_disabled"),
                        Identifier.of("woecrit", "renderer_button_highlighted")
                ),
                btn -> {
                    client.player.sendMessage(Text.literal("Toggle Renderer!"), true);
                    GhostRender.toggle();
                    this.renderButton.setTooltip(Tooltip.of(
                            GhostRender.isEnabled() ?
                                    Text.translatable(
                                            "options.on.composed",
                                            Text.translatable("woecrit_screen.renderer.tooltip")
                                    ) :
                                    Text.translatable(
                                            "options.off.composed",
                                            Text.translatable("woecrit_screen.renderer.tooltip")
                                    )
                    ));
                }
        );
        this.renderButton.setTooltip(Tooltip.of(
                GhostRender.isEnabled() ?
                        Text.translatable(
                                "options.on.composed",
                                Text.translatable("woecrit_screen.renderer.tooltip")
                        ) :
                        Text.translatable(
                                "options.off.composed",
                                Text.translatable("woecrit_screen.renderer.tooltip")
                        )
        ));
        this.addDrawableChild(this.renderButton);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void renderDarkening(DrawContext context) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Screen Move
        if (client.options.forwardKey.matchesKey(keyCode, scanCode)) {
            client.options.forwardKey.setPressed(true);
        }

        if (client.options.leftKey.matchesKey(keyCode, scanCode)) {
            client.options.leftKey.setPressed(true);
        }

        if (client.options.rightKey.matchesKey(keyCode, scanCode)) {
            client.options.rightKey.setPressed(true);
        }

        if (client.options.backKey.matchesKey(keyCode, scanCode)) {
            client.options.backKey.setPressed(true);
        }

        // Capture space first
        if (client.options.jumpKey.matchesKey(keyCode, scanCode)) {
            client.options.jumpKey.setPressed(true);
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (WoecritClient.TOGGLE_MAIN_SCREEN.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }


        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (client.options.forwardKey.matchesKey(keyCode, scanCode)) {
            client.options.forwardKey.setPressed(false);
        }

        if (client.options.leftKey.matchesKey(keyCode, scanCode)) {
            client.options.leftKey.setPressed(false);
        }

        if (client.options.rightKey.matchesKey(keyCode, scanCode)) {
            client.options.rightKey.setPressed(false);
        }

        if (client.options.backKey.matchesKey(keyCode, scanCode)) {
            client.options.backKey.setPressed(false);
        }

        if (client.options.jumpKey.matchesKey(keyCode, scanCode)) {
            client.options.jumpKey.setPressed(false);
        } else return super.keyReleased(keyCode, scanCode, modifiers);

        return false;
    }
}
