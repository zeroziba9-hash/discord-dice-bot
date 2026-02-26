package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends ListenerAdapter {

    private static final String BTN_OPEN_RANDOM_MODAL = "rand:open"; // backward compatibility
    private static final String BTN_ROLL_PREFIX = "rand:roll:";
    private static final String BTN_SET_PREFIX = "rand:set:";
    private static final String MODAL_RANDOM_PREFIX = "rand:modal:";

    public static void main(String[] args) throws InterruptedException {
        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {
            System.err.println("DISCORD_TOKEN 환경변수를 설정해줘.");
            System.err.println("PowerShell 예시: $env:DISCORD_TOKEN='여기에_토큰'");
            return;
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Main())
                .build();

        jda.awaitReady();

        jda.updateCommands().addCommands(
                Commands.slash("randomui", "버튼 + 입력창으로 랜덤 숫자 생성기 열기"),
                Commands.slash("random", "랜덤 숫자 생성")
                        .addOption(OptionType.INTEGER, "min", "최소값 (기본 1)", false)
                        .addOption(OptionType.INTEGER, "max", "최대값 (기본 999)", false)
        ).queue();

        System.out.println("봇 실행됨: " + jda.getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("randomui")) {
            event.replyEmbeds(buildPanelEmbed(1, 999, null).build())
                    .addActionRow(buildRollButton(1, 999), buildSetButton(1, 999))
                    .queue();
            return;
        }

        if (event.getName().equals("random")) {
            int min = event.getOption("min") != null ? event.getOption("min").getAsInt() : 1;
            int max = event.getOption("max") != null ? event.getOption("max").getAsInt() : 999;

            String error = validateRange(min, max);
            if (error != null) {
                event.reply("❌ " + error).setEphemeral(true).queue();
                return;
            }

            int n = roll(min, max);
            event.reply("🎲 결과: **" + n + "**  (" + min + "~" + max + ")").queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (BTN_OPEN_RANDOM_MODAL.equals(id)) {
            showRangeModal(event, 1, 999);
            return;
        }

        if (id.startsWith(BTN_ROLL_PREFIX)) {
            int[] range = parseRangeFromId(id, BTN_ROLL_PREFIX, 1, 999);
            int min = range[0];
            int max = range[1];
            int n = roll(min, max);

            event.editMessageEmbeds(buildPanelEmbed(min, max, n).build())
                    .setActionRow(buildRollButton(min, max), buildSetButton(min, max))
                    .queue();
            return;
        }

        if (id.startsWith(BTN_SET_PREFIX)) {
            int[] range = parseRangeFromId(id, BTN_SET_PREFIX, 1, 999);
            showRangeModal(event, range[0], range[1]);
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.startsWith(MODAL_RANDOM_PREFIX)) return;

        int[] currentRange = parseRangeFromId(modalId, MODAL_RANDOM_PREFIX, 1, 999);
        int min = currentRange[0];
        int max = currentRange[1];

        String minRaw = event.getValue("min") != null ? event.getValue("min").getAsString().trim() : "";
        String maxRaw = event.getValue("max") != null ? event.getValue("max").getAsString().trim() : "";

        try {
            if (!minRaw.isEmpty()) min = Integer.parseInt(minRaw);
            if (!maxRaw.isEmpty()) max = Integer.parseInt(maxRaw);
        } catch (NumberFormatException e) {
            event.reply("❌ 숫자만 입력해줘. 예: min=1, max=999").setEphemeral(true).queue();
            return;
        }

        String error = validateRange(min, max);
        if (error != null) {
            event.reply("❌ " + error).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(buildPanelEmbed(min, max, null).build())
                .addActionRow(buildRollButton(min, max), buildSetButton(min, max))
                .queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw().trim();

        if (content.equals("/randomui")) {
            event.getChannel().sendMessageEmbeds(buildPanelEmbed(1, 999, null).build())
                    .setActionRow(buildRollButton(1, 999), buildSetButton(1, 999))
                    .queue();
            return;
        }

        if (content.equals("!랜덤")) {
            int n = roll(1, 999);
            event.getChannel().sendMessage("🎲 " + n + " (1~999)").queue();
            return;
        }

        if (content.startsWith("!랜덤 ")) {
            String[] parts = content.split("\\s+");
            if (parts.length != 3) {
                event.getChannel().sendMessage("사용법: !랜덤 또는 !랜덤 최소 최대 (예: !랜덤 5 20)").queue();
                return;
            }

            try {
                int min = Integer.parseInt(parts[1]);
                int max = Integer.parseInt(parts[2]);

                String error = validateRange(min, max);
                if (error != null) {
                    event.getChannel().sendMessage("❌ " + error).queue();
                    return;
                }

                int n = roll(min, max);
                event.getChannel().sendMessage("🎲 " + n + "  (" + min + "~" + max + ")").queue();
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("❌ 숫자로 입력해줘. 예: !랜덤 1 100").queue();
            }
        }
    }

    private void showRangeModal(ButtonInteractionEvent event, int min, int max) {
        TextInput minInput = TextInput.create("min", "최소값 (기본 1)", TextInputStyle.SHORT)
                .setPlaceholder("1")
                .setValue(String.valueOf(min))
                .setRequired(false)
                .build();

        TextInput maxInput = TextInput.create("max", "최대값 (기본 999)", TextInputStyle.SHORT)
                .setPlaceholder("999")
                .setValue(String.valueOf(max))
                .setRequired(false)
                .build();

        Modal modal = Modal.create(MODAL_RANDOM_PREFIX + min + ":" + max, "랜덤 숫자 범위 설정")
                .addComponents(ActionRow.of(minInput), ActionRow.of(maxInput))
                .build();

        event.replyModal(modal).queue();
    }

    private Button buildRollButton(int min, int max) {
        return Button.primary(BTN_ROLL_PREFIX + min + ":" + max, "🎲 굴리기");
    }

    private Button buildSetButton(int min, int max) {
        return Button.secondary(BTN_SET_PREFIX + min + ":" + max, "⚙ 범위설정");
    }

    private EmbedBuilder buildPanelEmbed(int min, int max, Integer lastResult) {
        String desc = "현재 범위: **" + min + " ~ " + max + "**\n"
                + "- `🎲 굴리기`: 같은 범위로 계속 생성\n"
                + "- `⚙ 범위설정`: 최소/최대 변경";

        if (lastResult != null) {
            desc = "**결과: " + lastResult + "**\n" + desc;
        }

        return new EmbedBuilder()
                .setTitle("🎲 랜덤 숫자 생성기")
                .setDescription(desc)
                .setColor(new Color(88, 101, 242));
    }

    private int roll(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private int[] parseRangeFromId(String id, String prefix, int defaultMin, int defaultMax) {
        try {
            String payload = id.substring(prefix.length());
            String[] split = payload.split(":");
            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);
            return new int[]{min, max};
        } catch (Exception ignored) {
            return new int[]{defaultMin, defaultMax};
        }
    }

    private String validateRange(int min, int max) {
        if (min < 1) return "최소값은 1 이상이어야 해.";
        if (max > 999) return "최대값은 999 이하여야 해.";
        if (min > max) return "최소값은 최대값보다 클 수 없어.";
        return null;
    }
}
