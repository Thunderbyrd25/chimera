package com.chimera.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.chimera.ChimeraEntityTypes;
import com.chimera.ChimeraMenus;
import com.chimera.ChimeraMod;
import com.chimera.entity.WebHookRenderer;
import com.chimera.gene.TraitDisplay;
import com.chimera.item.WebSlingerItem;
import com.chimera.machine.BioreactorScreen;
import com.chimera.machine.CentrifugeScreen;
import com.chimera.machine.GeneExtractorScreen;
import com.chimera.machine.GeneSequencerScreen;
import com.chimera.machine.GenomeAnalyzerScreen;
import com.chimera.machine.GenomeSplicerScreen;
import com.chimera.machine.SynthesizerScreen;
import com.chimera.network.BiopediaEntry;
import com.chimera.network.BiopediaEntry.BiopediaEntryDetails;
import com.chimera.network.GrassFedUsePayload;
import com.chimera.network.OathResponsePayload;
import com.chimera.network.OpenBiopediaScreenEvent;
import com.chimera.network.OpenOathPromptEvent;
import com.chimera.network.RetractWebSlingerPayload;
import com.chimera.splice.SpliceCoreScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = ChimeraMod.MODID, dist = Dist.CLIENT)
public class ChimeraModClient {

    public static final KeyMapping GRASS_FED_KEY =
            new KeyMapping("key.chimera.grass_fed", GLFW.GLFW_KEY_G, "key.categories.chimera");

    public ChimeraModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerEntityRenderers);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onOpenOathPrompt);
        NeoForge.EVENT_BUS.addListener(this::onOpenBiopedia);
        NeoForge.EVENT_BUS.addListener(this::onAttackKey);
    }

    // Byproduct economy work order Milestone 3b: WebHookRenderer delegates the spinning-icon
    // rendering to a held ThrownItemRenderer and additionally draws the string - see that class.
    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ChimeraEntityTypes.WEB_HOOK.get(), WebHookRenderer::new);
    }

    // Byproduct economy work order Milestone 3b: left-click retract. isAttack() fires for every
    // left-click regardless of target (block, entity, or empty air) - PlayerInteractEvent.
    // LeftClickEmpty was considered first but is explicitly client-only and empty-air-only per
    // its own javadoc, insufficient here. Doesn't cancel the event or suppress the vanilla swing
    // - retracting is additive, so punching something while holding the Web Slinger still works
    // normally and also retracts any active hook.
    private void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof WebSlingerItem || player.getOffhandItem().getItem() instanceof WebSlingerItem) {
            PacketDistributor.sendToServer(new RetractWebSlingerPayload());
        }
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ChimeraMenus.GENE_SEQUENCER.get(), GeneSequencerScreen::new);
        event.register(ChimeraMenus.GENOME_ANALYZER.get(), GenomeAnalyzerScreen::new);
        event.register(ChimeraMenus.GENE_EXTRACTOR.get(), GeneExtractorScreen::new);
        event.register(ChimeraMenus.CENTRIFUGE.get(), CentrifugeScreen::new);
        event.register(ChimeraMenus.GENOME_SPLICER.get(), GenomeSplicerScreen::new);
        event.register(ChimeraMenus.BIOREACTOR.get(), BioreactorScreen::new);
        event.register(ChimeraMenus.SYNTHESIZER.get(), SynthesizerScreen::new);
        event.register(ChimeraMenus.SPLICE_CORE.get(), SpliceCoreScreen::new);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GRASS_FED_KEY);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        while (GRASS_FED_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new GrassFedUsePayload());
        }
    }

    // Biopedia+Oath work order Milestone 2 - see OpenOathPromptEvent for why this indirection
    // exists (TheOathItem itself must never reference Screen/ConfirmScreen/Minecraft).
    private void onOpenOathPrompt(OpenOathPromptEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(
                accepted -> {
                    PacketDistributor.sendToServer(new OathResponsePayload(event.hand(), accepted));
                    minecraft.setScreen(null);
                },
                Component.translatable("screen.chimera.the_oath.title"),
                Component.translatable("screen.chimera.the_oath.message")));
    }

    // Matches BookViewScreen's own TEXT_WIDTH/TEXT_HEIGHT (both protected, so re-declared here
    // rather than accessed directly) - a single page is not guaranteed to fit a gene's full
    // entry (the inert notice paragraph alone runs to ~10 wrapped lines), so entries are
    // measured with the real font and split across multiple pages rather than assumed to fit.
    // Deliberately more conservative than BookViewScreen's own protected TEXT_WIDTH=114/
    // TEXT_HEIGHT=128 constants - an earlier attempt at 114/13 still overflowed a page in
    // practice (see paginate()'s own comment), so this errs toward pagination happening too
    // early rather than clipping text again.
    private static final int PAGE_TEXT_WIDTH = 100;
    private static final int MAX_LINES_PER_PAGE = 10;

    // Biopedia+Oath work order Milestone 3 - see OpenBiopediaScreenEvent for why this
    // indirection exists. The server sent plain data (BiopediaEntry); this is the one place
    // that turns it into styled Components and opens the vanilla book reader.
    private void onOpenBiopedia(OpenBiopediaScreenEvent event) {
        List<Component> pages = new ArrayList<>();
        for (BiopediaEntry entry : event.entries()) {
            pages.addAll(buildPages(entry));
        }
        Minecraft.getInstance().setScreen(new BookViewScreen(new BookViewScreen.BookAccess(pages)));
    }

    private static List<Component> buildPages(BiopediaEntry entry) {
        if (entry.details().isEmpty()) {
            Component locked = Component.translatable("biopedia.chimera.unidentified").copy()
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY)
                    .append("\n\n")
                    .append(Component.translatable("biopedia.chimera.tier", entry.tier()).withStyle(ChatFormatting.DARK_GRAY));
            return List.of(locked);
        }

        BiopediaEntryDetails details = entry.details().get();
        Component name = TraitDisplay.traitName(entry.geneId()).copy().withStyle(ChatFormatting.BOLD, ChatFormatting.BLACK);

        // Each entry here is one self-contained "paragraph" - never split mid-paragraph, only
        // ever moved whole onto the next page if it wouldn't fit on the current one.
        List<Component> paragraphs = new ArrayList<>();
        paragraphs.add(name.copy().append("\n")
                .append(Component.translatable("biopedia.chimera.tier", entry.tier()).withStyle(ChatFormatting.DARK_GRAY)));

        if (!details.mobs().isEmpty()) {
            MutableComponent mobNames = null;
            for (ResourceLocation mobId : details.mobs()) {
                Component mobName = BuiltInRegistries.ENTITY_TYPE.containsKey(mobId)
                        ? BuiltInRegistries.ENTITY_TYPE.get(mobId).getDescription()
                        : Component.literal(mobId.toString());
                mobNames = mobNames == null ? mobName.copy() : mobNames.append(", ").append(mobName);
            }
            paragraphs.add(Component.translatable("biopedia.chimera.mobs", mobNames).copy().withStyle(ChatFormatting.DARK_GRAY));
        }

        for (String upside : details.upsideLines()) {
            paragraphs.add(Component.literal(upside).copy().withStyle(ChatFormatting.GRAY));
        }
        for (String drawback : details.drawbackLines()) {
            paragraphs.add(Component.literal(drawback).copy().withStyle(ChatFormatting.RED));
        }
        if (details.requiresAnima()) {
            // Split into sentences rather than one long paragraph, so pagination has room to
            // pack the earlier sentences onto a page with space left over instead of treating
            // the whole notice as one atomic, unsplittable block.
            for (String sentence : splitIntoSentences(Component.translatable("biopedia.chimera.inert_notice").getString())) {
                paragraphs.add(Component.literal(sentence).copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }

        return paginate(name, paragraphs);
    }

    private static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        for (String sentence : text.split("(?<=[.!?])\\s+")) {
            if (!sentence.isBlank()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }

    // Packs paragraphs onto pages by measuring the actual combined candidate page with the real
    // font each time (not summing separately-estimated line counts, which proved inaccurate in
    // practice - a page came back visibly underfull while the very next page still overflowed).
    // PAGE_TEXT_WIDTH/MAX_LINES_PER_PAGE are deliberately conservative versus BookViewScreen's
    // own (protected, unreachable) TEXT_WIDTH/TEXT_HEIGHT - better to paginate a little early
    // than to silently clip text again. Continuation pages repeat the gene's name so an overflow
    // page is never orphaned/unidentifiable.
    private static List<Component> paginate(Component name, List<Component> paragraphs) {
        Font font = Minecraft.getInstance().font;
        List<Component> pages = new ArrayList<>();
        List<Component> current = new ArrayList<>();
        boolean firstPage = true;

        for (Component paragraph : paragraphs) {
            List<Component> candidate = new ArrayList<>(current);
            candidate.add(paragraph);
            boolean fits = current.isEmpty()
                    || font.split(joinPage(name, firstPage, candidate), PAGE_TEXT_WIDTH).size() <= MAX_LINES_PER_PAGE;
            if (fits) {
                current = candidate;
            } else {
                pages.add(joinPage(name, firstPage, current));
                firstPage = false;
                current = new ArrayList<>();
                current.add(paragraph);
            }
        }
        if (!current.isEmpty()) {
            pages.add(joinPage(name, firstPage, current));
        }
        return pages;
    }

    private static Component joinPage(Component name, boolean firstPage, List<Component> paragraphs) {
        MutableComponent page = firstPage ? Component.empty() : name.copy().append(" (cont.)\n\n");
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                page.append("\n\n");
            }
            page.append(paragraphs.get(i));
        }
        return page;
    }
}
