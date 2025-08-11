package com.teamphacode.MerchantManagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class MerchantManagementApplicationTests {

	@Test
	void loadTestMillionRequests() throws InterruptedException {
		int totalRequests = 1000;
		int concurrentThreads = 500; // giới hạn luồng đồng thời (máy yếu nên để thấp)
		ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
		CountDownLatch latch = new CountDownLatch(totalRequests);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		final String accountNo = "1234567890123456782";

		for (int i = 0; i < totalRequests; i++) {
			final int requestId = i + 1;

			executor.submit(() -> {
				try {
					String jsonInput = """
                        {
                            "accountNo": "%s",
                            "fullName": "CONG TY TNHH PHAT TRIEN ABC",
                            "shortName": "ABC CO",
                            "mcc": "5411",
                            "city": "HANOI",
                            "location": "CAU GIAY",
                            "phoneNo": "0912345678",
                            "email": "contact@abc.com",
                            "openDate": "2023-01-01",
                            "closeDate": null,
                            "status": "ACTIVE"
                        }
                        """.formatted(accountNo);

					URL url = new URL("http://localhost:8080/api/v1/merchant/update");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("PUT");
					conn.setRequestProperty("Content-Type", "application/json");
					conn.setDoOutput(true);

					long start = System.currentTimeMillis();

					try (OutputStream os = conn.getOutputStream()) {
						byte[] input = jsonInput.getBytes("utf-8");
						os.write(input, 0, input.length);
					}

					int responseCode = conn.getResponseCode();
					long duration = System.currentTimeMillis() - start;

					if (responseCode == 200) {
						successCount.incrementAndGet();
					} else {
						failureCount.incrementAndGet();
					}

					if (requestId % 10000 == 0) {
						System.out.printf("Đã gửi %d request%n", requestId);
					}

				} catch (Exception e) {
					failureCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		System.out.println("\n========= TỔNG KẾT =========");
		System.out.println("✅ Thành công: " + successCount.get());
		System.out.println("❌ Thất bại: " + failureCount.get());
		System.out.println("🎯 Tổng request: " + totalRequests);
		System.out.println("✅ Load test hoàn tất!");
	}
}
