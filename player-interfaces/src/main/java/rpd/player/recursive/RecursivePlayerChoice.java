package rpd.player.recursive;

import rpd.player.Option;

public record RecursivePlayerChoice(Option option, RecursivePlayerStep nextStep) {
}
