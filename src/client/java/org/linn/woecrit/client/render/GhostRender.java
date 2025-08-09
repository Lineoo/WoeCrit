package org.linn.woecrit.client.render;

import org.linn.woecrit.client.world.GhostWorld;

public class GhostRender {
    private static boolean enable = true;

    public static boolean isEnabled() {
        return enable;
    }

    public static void toggle() {
        enable = !enable;
    }

    public GhostBlockRender blockRender;

    public GhostRender(GhostWorld world) {
        blockRender = new GhostBlockRender(world);
    }
}
