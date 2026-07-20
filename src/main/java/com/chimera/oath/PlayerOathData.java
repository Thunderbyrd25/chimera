package com.chimera.oath;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Player identity state for the Oath (Biopedia+Oath work order Milestone 1) - the foundation for
// the mod's whole back-half arc (Scientist -> Reaper -> Seraph), so the shape is built for all
// three acts now even though only hasOath/SCIENTIST get set this milestone. oathBroken and
// REAPER/SERAPH are dormant - nothing reads or writes them yet (see OathPath).
public record PlayerOathData(boolean hasOath, boolean oathBroken, OathPath path) {

    public static final PlayerOathData EMPTY = new PlayerOathData(false, false, OathPath.NONE);

    public static final Codec<PlayerOathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("has_oath").forGetter(PlayerOathData::hasOath),
            Codec.BOOL.fieldOf("oath_broken").forGetter(PlayerOathData::oathBroken),
            OathPath.CODEC.fieldOf("path").forGetter(PlayerOathData::path)
    ).apply(instance, PlayerOathData::new));

    public PlayerOathData withOathTaken() {
        return new PlayerOathData(true, oathBroken, OathPath.SCIENTIST);
    }
}
