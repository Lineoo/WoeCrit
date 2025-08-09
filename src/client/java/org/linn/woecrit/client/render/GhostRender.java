package org.linn.woecrit.client.render;

import org.linn.woecrit.client.world.GhostWorld;

public class GhostRender {
    public GhostBlockRender blockRender;

    public GhostRender(GhostWorld world) {
        blockRender = new GhostBlockRender(world);
    }
}
