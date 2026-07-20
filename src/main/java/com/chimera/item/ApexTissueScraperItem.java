package com.chimera.item;

// The tier-2 unlock: a strict upgrade over Reinforced (higher bonus-sample chance), also able to
// sample tier-2 gene pools (horse/zombie/skeleton/spider). Crafted from Refined Culture, the
// Bioreactor's output - see BioreactorBlockEntity.
public class ApexTissueScraperItem extends ReinforcedTissueScraperItem {

    private static final float BONUS_SAMPLE_CHANCE = 0.4F;

    public ApexTissueScraperItem(Properties properties) {
        super(properties);
    }

    @Override
    protected float bonusSampleChance() {
        return BONUS_SAMPLE_CHANCE;
    }

    @Override
    protected int maxTier() {
        return 2;
    }
}
