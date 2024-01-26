package nu.studer.teamcity.buildscan;

import io.github.dsibilio.badgemaker.core.BadgeFormatBuilder;
import io.github.dsibilio.badgemaker.core.BadgeMaker;
import io.github.dsibilio.badgemaker.model.BadgeFormat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BuildScanBadge {
    private static final String GRADLE_LOGO = "data:image/svg+xml;base64,PHN2ZyBmaWxsPSJ3aGl0ZXNtb2tlIiByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48dGl0bGU+R3JhZGxlPC90aXRsZT48cGF0aCBkPSJNMjIuNjk1IDQuMjk3YTMuODA3IDMuODA3IDAgMCAwLTUuMjktLjA5LjM2OC4zNjggMCAwIDAgMCAuNTMzbC40Ni40N2EuMzYzLjM2MyAwIDAgMCAuNDc0LjAzMiAyLjE4MiAyLjE4MiAwIDAgMSAyLjg2IDMuMjkxYy0zLjAyMyAzLjAyLTcuMDU2LTUuNDQ3LTE2LjIxMS0xLjA4M2ExLjI0IDEuMjQgMCAwIDAtLjUzNCAxLjc0NWwxLjU3MSAyLjcxM2ExLjIzOCAxLjIzOCAwIDAgMCAxLjY4MS40NjFsLjAzNy0uMDItLjAyOS4wMi42ODgtLjM4NGExNi4wODMgMTYuMDgzIDAgMCAwIDIuMTkzLTEuNjM1LjM4NC4zODQgMCAwIDEgLjQ5OS0uMDE2LjM1Ny4zNTcgMCAwIDEgLjAxNi41MzQgMTYuNDM1IDE2LjQzNSAwIDAgMS0yLjMxNiAxLjc0MUg4Ljc3bC0uNjk2LjM5YTEuOTU4IDEuOTU4IDAgMCAxLS45NjMuMjUgMS45ODcgMS45ODcgMCAwIDEtMS43MjYtLjk4OUwzLjkgOS42OTZDMS4wNiAxMS43Mi0uNjg2IDE1LjYwMy4yNiAyMC41MjJhLjM2My4zNjMgMCAwIDAgLjM1NC4yOTZoMS42NzVhLjM2My4zNjMgMCAwIDAgLjM3LS4zMzEgMi40NzggMi40NzggMCAwIDEgNC45MTUgMCAuMzYuMzYgMCAwIDAgLjM1Ny4zMTdoMS42MzhhLjM2My4zNjMgMCAwIDAgLjM1Ny0uMzE3IDIuNDc4IDIuNDc4IDAgMCAxIDQuOTE0IDAgLjM2My4zNjMgMCAwIDAgLjM1OC4zMTdoMS42MjdhLjM2My4zNjMgMCAwIDAgLjM2My0uMzU3Yy4wMzctMi4yOTQuNjU2LTQuOTMgMi40Mi02LjI1IDYuMTA4LTQuNTcgNC41MDItOC40ODYgMy4wODgtOS45em0tNi4yMjkgNi45MDFsLTEuMTY1LS41ODRhLjczLjczIDAgMSAxIDEuMTY1LjU4N3oiLz48L3N2Zz4=";

    static String create(BuildScanReference reference) {
        BadgeFormat badgeFormat = new BadgeFormatBuilder(reference.getUrlWithoutProtocol())
            .withLogo(GRADLE_LOGO)
            .withLabel("Build Scan®️")
            .withMessageColor(() -> "#06A0CE")
            .build();
        return BadgeMaker.makeBadge(badgeFormat);
    }

    static String createEncoded(BuildScanReference reference) {
        return encodeToDataUrl(create(reference));
    }

    static String encodeToDataUrl(String imgSvg) {
        // don't use getUrlEncoder() for data URLs
        return "data:image/svg+xml;base64,"+ Base64.getEncoder().encodeToString(imgSvg.getBytes(StandardCharsets.UTF_8));
    }
}
