package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.ThreadLocalRandom;

public class Main extends ListenerAdapter {

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
        System.out.println("봇 실행됨: " + jda.getSelfUser().getAsTag());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw().trim();

        if (content.equals("!랜덤")) {
            int n = ThreadLocalRandom.current().nextInt(1, 11); // 1~10
            event.getChannel().sendMessage("🎲 " + n).queue();
            return;
        }

        if (content.startsWith("!랜덤 ")) {
            // !랜덤 5 20
            String[] parts = content.split("\\s+");
            if (parts.length != 3) {
                event.getChannel().sendMessage("사용법: !랜덤 또는 !랜덤 최소 최대 (예: !랜덤 5 20)").queue();
                return;
            }

            try {
                int min = Integer.parseInt(parts[1]);
                int max = Integer.parseInt(parts[2]);

                if (min > max) {
                    event.getChannel().sendMessage("❌ 최소값은 최대값보다 클 수 없어.").queue();
                    return;
                }

                int n = ThreadLocalRandom.current().nextInt(min, max + 1);
                event.getChannel().sendMessage("🎲 " + n + "  (" + min + "~" + max + ")").queue();
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("❌ 숫자로 입력해줘. 예: !랜덤 1 100").queue();
            }
        }
    }
}
