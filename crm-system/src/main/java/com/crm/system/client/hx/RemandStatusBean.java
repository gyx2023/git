package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandStatusBean complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandStatusBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="remandCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remandState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currentLink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currentLinkDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="staffName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mobilPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandStatusBean", propOrder = { "remandCode", "remandState", "currentLink", "currentLinkDate",
		"staffName", "mobilPhone" })
public class RemandStatusBean {

	protected String remandCode;
	protected String remandState;
	protected String currentLink;
	protected String currentLinkDate;
	protected String staffName;
	protected String mobilPhone;

	/**
	 * ��ȡremandCode���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getRemandCode() {
		return remandCode;
	}

	/**
	 * ����remandCode���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setRemandCode(String value) {
		this.remandCode = value;
	}

	/**
	 * ��ȡremandState���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getRemandState() {
		return remandState;
	}

	/**
	 * ����remandState���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setRemandState(String value) {
		this.remandState = value;
	}

	/**
	 * ��ȡcurrentLink���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getCurrentLink() {
		return currentLink;
	}

	/**
	 * ����currentLink���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setCurrentLink(String value) {
		this.currentLink = value;
	}

	/**
	 * ��ȡcurrentLinkDate���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getCurrentLinkDate() {
		return currentLinkDate;
	}

	/**
	 * ����currentLinkDate���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setCurrentLinkDate(String value) {
		this.currentLinkDate = value;
	}

	/**
	 * ��ȡstaffName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getStaffName() {
		return staffName;
	}

	/**
	 * ����staffName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setStaffName(String value) {
		this.staffName = value;
	}

	/**
	 * ��ȡmobilPhone���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getMobilPhone() {
		return mobilPhone;
	}

	/**
	 * ����mobilPhone���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setMobilPhone(String value) {
		this.mobilPhone = value;
	}

}
