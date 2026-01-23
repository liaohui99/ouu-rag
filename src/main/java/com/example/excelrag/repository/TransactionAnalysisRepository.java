package com.example.excelrag.repository;

import com.example.excelrag.model.entity.TransactionAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 成交分析数据访问接口
 * 提供数据库操作方法
 */
@Repository
public interface TransactionAnalysisRepository extends JpaRepository<TransactionAnalysis, Long> {

    /**
     * 根据商品名称查询成交记录
     *
     * @param productName 商品名称
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findByProductName(String productName);

    /**
     * 根据商品类别查询成交记录
     *
     * @param productCategory 商品类别
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findByProductCategory(String productCategory);

    /**
     * 根据客户名称查询成交记录
     *
     * @param customerName 客户名称
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findByCustomerName(String customerName);

    /**
     * 根据销售渠道查询成交记录
     *
     * @param salesChannel 销售渠道
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findBySalesChannel(String salesChannel);

    /**
     * 根据地区查询成交记录
     *
     * @param region 地区
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findByRegion(String region);

    /**
     * 根据销售人员查询成交记录
     *
     * @param salesPerson 销售人员
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findBySalesPerson(String salesPerson);

    /**
     * 查询指定金额范围内的成交记录
     *
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @return 成交记录列表
     */
    List<TransactionAnalysis> findByAmountBetween(Double minAmount, Double maxAmount);

    /**
     * 统计总成交金额
     *
     * @return 总成交金额
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionAnalysis t")
    Double sumTotalAmount();

    /**
     * 统计总成交数量
     *
     * @return 总成交数量
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TransactionAnalysis t")
    Integer sumTotalQuantity();

    /**
     * 根据商品类别统计成交金额
     *
     * @return 商品类别和对应成交金额的列表
     */
    @Query("SELECT t.productCategory, SUM(t.amount) FROM TransactionAnalysis t GROUP BY t.productCategory")
    List<Object[]> sumAmountByCategory();

    /**
     * 根据销售渠道统计成交金额
     *
     * @return 销售渠道和对应成交金额的列表
     */
    @Query("SELECT t.salesChannel, SUM(t.amount) FROM TransactionAnalysis t GROUP BY t.salesChannel")
    List<Object[]> sumAmountBySalesChannel();

    /**
     * 根据地区统计成交金额
     *
     * @return 地区和对应成交金额的列表
     */
    @Query("SELECT t.region, SUM(t.amount) FROM TransactionAnalysis t GROUP BY t.region")
    List<Object[]> sumAmountByRegion();
}
