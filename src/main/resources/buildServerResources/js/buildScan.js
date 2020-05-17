BS.BuildScan = {
    startCleanup: function () {
        if (!confirm("This operation may require significant time, and the server will be less performant during the clean-up.\n" +
            "Are you sure you want to start clean-up process now?")) return false;
        $('cleanupResult').style.visibility = 'visible'
        $('startBuildScanCleanupButton').disabled = true
        $('cleanupResult').textContent = 'Cleanup is running...';
        $('progressRing').style.visibility = 'visible'
        BS.ajaxRequest($('cleanupCustomDataStorage').action, {
            method: 'POST',
            onComplete: function (res) {
                $('progressRing').style.visibility = 'hidden'
                $('startBuildScanCleanupButton').disabled = false
                if (res.status === 200) {
                    $('cleanupResult').textContent = 'Cleanup was successful';
                } else {
                    $('cleanupResult').textContent = 'Failed to execute cleanup - check the server logs'
                }
            }
        });
        return false;
    }
}