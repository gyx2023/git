package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * contractBean complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="contractBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractStartDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractEndDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractTypeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractNature" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractNatureName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="busiType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="busiTypeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractStateName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFixedAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxNotAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ourSubject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="otherFullName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="handleDep" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="handlePerson" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="handlePhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="approveDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractRemarks" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractBean", propOrder = { "contractCode", "contractName", "contractStartDate", "contractEndDate",
		"contractType", "contractTypeName", "contractNature", "contractNatureName", "busiType", "busiTypeName",
		"contractState", "contractStateName", "isFixedAmount", "taxAmount", "taxNotAmount", "ourSubject", "otherFullName",
		"handleDep", "handlePerson", "handlePhone", "approveDate", "contractRemarks","contractRelateState","contractRelateTime","contractCancelTime" })
public class ContractBean {

	protected String contractCode;
	protected String contractName;
	protected String contractStartDate;
	protected String contractEndDate;
	protected String contractType;
	protected String contractTypeName;
	protected String contractNature;
	protected String contractNatureName;
	protected String busiType;
	protected String busiTypeName;
	protected String contractState;
	protected String contractStateName;
	protected String isFixedAmount;
	protected String taxAmount;
	protected String taxNotAmount;
	protected String ourSubject;
	protected String otherFullName;
	protected String handleDep;
	protected String handlePerson;
	protected String handlePhone;
	protected String approveDate;
	protected String contractRemarks;
	protected String contractRelateState;
	protected String contractRelateTime;
	protected String contractCancelTime;


	/**
	 * ��ȡcontractCode���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractCode() {
		return contractCode;
	}

	/**
	 * ����contractCode���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractCode(String value) {
		this.contractCode = value;
	}

	/**
	 * ��ȡcontractName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractName() {
		return contractName;
	}

	/**
	 * ����contractName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractName(String value) {
		this.contractName = value;
	}

	/**
	 * ��ȡcontractStartDate���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractStartDate() {
		return contractStartDate;
	}

	/**
	 * ����contractStartDate���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractStartDate(String value) {
		this.contractStartDate = value;
	}

	/**
	 * ��ȡcontractEndDate���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractEndDate() {
		return contractEndDate;
	}

	/**
	 * ����contractEndDate���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractEndDate(String value) {
		this.contractEndDate = value;
	}

	/**
	 * ��ȡcontractType���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractType() {
		return contractType;
	}

	/**
	 * ����contractType���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractType(String value) {
		this.contractType = value;
	}

	/**
	 * ��ȡcontractTypeName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractTypeName() {
		return contractTypeName;
	}

	/**
	 * ����contractTypeName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractTypeName(String value) {
		this.contractTypeName = value;
	}

	/**
	 * ��ȡcontractNature���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractNature() {
		return contractNature;
	}

	/**
	 * ����contractNature���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractNature(String value) {
		this.contractNature = value;
	}

	/**
	 * ��ȡcontractNatureName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractNatureName() {
		return contractNatureName;
	}

	/**
	 * ����contractNatureName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractNatureName(String value) {
		this.contractNatureName = value;
	}

	/**
	 * ��ȡbusiType���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getBusiType() {
		return busiType;
	}

	/**
	 * ����busiType���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setBusiType(String value) {
		this.busiType = value;
	}

	/**
	 * ��ȡbusiTypeName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getBusiTypeName() {
		return busiTypeName;
	}

	/**
	 * ����busiTypeName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setBusiTypeName(String value) {
		this.busiTypeName = value;
	}

	/**
	 * ��ȡcontractState���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractState() {
		return contractState;
	}

	/**
	 * ����contractState���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractState(String value) {
		this.contractState = value;
	}

	/**
	 * ��ȡcontractStateName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractStateName() {
		return contractStateName;
	}

	/**
	 * ����contractStateName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractStateName(String value) {
		this.contractStateName = value;
	}

	/**
	 * ��ȡisFixedAmount���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getIsFixedAmount() {
		return isFixedAmount;
	}

	/**
	 * ����isFixedAmount���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setIsFixedAmount(String value) {
		this.isFixedAmount = value;
	}

	/**
	 * ��ȡtaxAmount���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getTaxAmount() {
		return taxAmount;
	}

	/**
	 * ����taxAmount���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setTaxAmount(String value) {
		this.taxAmount = value;
	}

	/**
	 * ��ȡtaxNotAmount���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getTaxNotAmount() {
		return taxNotAmount;
	}

	/**
	 * ����taxNotAmount���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setTaxNotAmount(String value) {
		this.taxNotAmount = value;
	}

	/**
	 * ��ȡourSubject���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getOurSubject() {
		return ourSubject;
	}

	/**
	 * ����ourSubject���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setOurSubject(String value) {
		this.ourSubject = value;
	}

	/**
	 * ��ȡotherFullName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getOtherFullName() {
		return otherFullName;
	}

	/**
	 * ����otherFullName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setOtherFullName(String value) {
		this.otherFullName = value;
	}

	/**
	 * ��ȡhandleDep���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getHandleDep() {
		return handleDep;
	}

	/**
	 * ����handleDep���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setHandleDep(String value) {
		this.handleDep = value;
	}

	/**
	 * ��ȡhandlePerson���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getHandlePerson() {
		return handlePerson;
	}

	/**
	 * ����handlePerson���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setHandlePerson(String value) {
		this.handlePerson = value;
	}

	/**
	 * ��ȡhandlePhone���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getHandlePhone() {
		return handlePhone;
	}

	/**
	 * ����handlePhone���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setHandlePhone(String value) {
		this.handlePhone = value;
	}

	/**
	 * ��ȡapproveDate���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getApproveDate() {
		return approveDate;
	}

	/**
	 * ����approveDate���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setApproveDate(String value) {
		this.approveDate = value;
	}

	/**
	 * ��ȡcontractRemarks���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getContractRemarks() {
		return contractRemarks;
	}

	/**
	 * ����contractRemarks���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setContractRemarks(String value) {
		this.contractRemarks = value;
	}

	public String getContractRelateState() {
		return contractRelateState;
	}

	public void setContractRelateState(String contractRelateState) {
		this.contractRelateState = contractRelateState;
	}

	public String getContractRelateTime() {
		return contractRelateTime;
	}

	public void setContractRelateTime(String contractRelateTime) {
		this.contractRelateTime = contractRelateTime;
	}

	public String getContractCancelTime() {
		return contractCancelTime;
	}

	public void setContractCancelTime(String contractCancelTime) {
		this.contractCancelTime = contractCancelTime;
	}
}
