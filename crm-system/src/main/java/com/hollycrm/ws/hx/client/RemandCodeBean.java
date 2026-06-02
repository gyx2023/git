package com.hollycrm.ws.hx.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandCodeBean complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandCodeBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oppoCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remandCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandCodeBean", propOrder = { "oppoCode", "remandCode" })
public class RemandCodeBean {

	protected String oppoCode;
	protected String remandCode;

	/**
	 * ��ȡoppoCode���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getOppoCode() {
		return oppoCode;
	}

	/**
	 * ����oppoCode���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setOppoCode(String value) {
		this.oppoCode = value;
	}

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

}
