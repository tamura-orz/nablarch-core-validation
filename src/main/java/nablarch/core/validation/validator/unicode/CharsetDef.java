package nablarch.core.validation.validator.unicode;

import nablarch.core.util.annotation.Published;

/**
 * 許容する文字の集合の定義する為のインタフェース。<br/>
 * 与えられたUnicodeコードポイントが許容文字であるか判定する責務を持つ。
 * 自身の文字集合に含まれている場合は真を返却する。
 * どのようなデータ構造で集合を定義するかは規定しない。
 *
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public interface CharsetDef {

    /**
     * コードポイントが許容文字であるか判定する。
     *
     * @param codePoint Unicodeコードポイント
     * @return 許容文字である場合、真
     */
    boolean contains(int codePoint);

    /**
     * 文字種チェックでエラーが発生した際にデフォルトで使用するメッセージIDを取得する。
     *
     * @return メッセージID
     */
    String getMessageId();
}
