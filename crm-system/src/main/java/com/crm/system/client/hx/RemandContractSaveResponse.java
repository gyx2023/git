package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandContractSaveResponse complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandContractSaveResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://service.oppo.sdunicomsi.com/}outParam" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandContractSaveResponse", propOrder = { "_return" })
public class RemandContractSaveResponse {

	@XmlElement(name = "return")
	protected OutParam _return;

	/**
	 * ��ȡreturn���Ե�ֵ��
	 *
	 * @return possible object is {@link OutParam }
	 *
	 */
	public OutParam getReturn() {
		return _return;
	}

	/**
	 * ����return���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link OutParam }
	 *
	 */
	public void setReturn(OutParam value) {
		this._return = value;
	}

}
