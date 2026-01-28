import com.example.excelrag.service.ExcelToTableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 10:32
 * @description: TODO
 */
@SpringBootTest
class ExcelToTableServiceTest {

    @Autowired
    private ExcelToTableService service;
    @Autowired private DataSource dataSource;

    @Test
    void testProcessExcel() throws Exception {
        // 准备测试Excel文件
        File file = new File("src/test/resources/test_data.xlsx");
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test_data.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fis
        );

        // 执行
        String tableName = service.processExcelAndTrain(mockFile, "test_import", 0);

        // 验证表已创建
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM " + tableName);
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0);
        }
    }
}
