package com.Django.TraceChain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Django.TraceChain.model.Wallet;

@Service
public class DetectService {
	
	private final List<MixingDetector> detectors;

	@Autowired
    public DetectService(List<MixingDetector> detectors) {
        this.detectors = detectors;
    }

	public void runAllDetectors(List<Wallet> wallets) {
	    if (wallets.isEmpty()) return;

	    int type = wallets.get(0).getType();

	    for (MixingDetector detector : detectors) {
	        if (type == 2 && detector instanceof PeelChainDetector) {
	            continue;  // type 0이면 PeelChainDetector 제외
	        }
	        if (type == 1 && detector instanceof RelayerDetector) {
	            continue;  // type 1이면 RelayerDetector 제외
	        }
	        detector.analyze(wallets);
	    }
	}

    //임시코드
    public void runLoopingOnly(List<Wallet> wallets) {
        for (MixingDetector detector : detectors) {
            if (detector instanceof LoopingDetector) {
                detector.analyze(wallets);
            }
        }
    }

}
