package com.chimera.oath;

import com.mojang.serialization.Codec;

// The Oath's spine (Biopedia+Oath work order Milestone 1). REAPER/SERAPH are dormant past
// SCIENTIST - nothing sets or reads them yet, reserved so the Reaper/Seraph acts don't need a
// schema migration when they land.
public enum OathPath {
    NONE, SCIENTIST, REAPER, SERAPH;

    public static final Codec<OathPath> CODEC = Codec.STRING.xmap(OathPath::valueOf, Enum::name);
}
