package com.chimera.item;

// The Phase 7 upgrade path: more durability, plus a chance at a bonus sample per scrape.
public class ReinforcedTissueScraperItem extends TissueScraperItem {

    private static final float BONUS_SAMPLE_CHANCE = 0.25F;

    public ReinforcedTissueScraperItem(Properties properties) {
        super(properties);
    }

    @Override
    protected float bonusSampleChance() {
        return BONUS_SAMPLE_CHANCE;
    }
}
