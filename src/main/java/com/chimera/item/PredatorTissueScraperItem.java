package com.chimera.item;

// The tier-3 unlock, mirroring exactly how ApexTissueScraperItem unlocked tier 2: a strict
// upgrade over Apex (higher bonus-sample chance), able to sample tier-3 gene pools. Crafted
// from Combat Stimulant - see ChimeraRecipeProvider - which in turn requires Stress Plasma,
// only obtainable by scraping a Potion-of-Stress-marked mob (see TissueScraperItem).
public class PredatorTissueScraperItem extends ApexTissueScraperItem {

    private static final float BONUS_SAMPLE_CHANCE = 0.55F;

    public PredatorTissueScraperItem(Properties properties) {
        super(properties);
    }

    @Override
    protected float bonusSampleChance() {
        return BONUS_SAMPLE_CHANCE;
    }

    @Override
    protected int maxTier() {
        return 3;
    }
}
