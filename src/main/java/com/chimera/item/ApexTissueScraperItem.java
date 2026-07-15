package com.chimera.item;

// The tier-2 unlock: a strict upgrade over Reinforced (inherits its bonus-sample chance), also
// able to sample tier-2 gene pools (horse/zombie/skeleton/spider). Crafted from Refined Culture,
// the Bioreactor's output - see BioreactorBlockEntity.
public class ApexTissueScraperItem extends ReinforcedTissueScraperItem {

    public ApexTissueScraperItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int maxTier() {
        return 2;
    }
}
