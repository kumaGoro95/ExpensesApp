# Piggy Bank
 
 「お金でわからないことはすぐ相談できる」チャット相談機能付家計簿アプリ。家計簿機能と質問機能を実装しています。
  
[![Image from Gyazo](https://i.gyazo.com/987d86e72e7e6db6e69a2790d79f9955.gif)](https://gyazo.com/987d86e72e7e6db6e69a2790d79f9955)

 
# URL
 
 https://piggy-bank-app.com/
 
 ログインボタン下のリンクから、メールアドレスとパスワードを入力せずにログインできます。
 
 
# 制作背景
私は夫と二人暮らし＆共働きだったのですが、半年前、夫が体調を崩して休職に入りました。それまでお金についてはかなり大雑把に管理しており、夫の休職によって家計管理・節約をきっちりやる必要が出てきました。様々な家計簿アプリを探してみたのですが、その中で「ユーザーとしてこういうものが欲しい」という希望が浮かび上がってきました。

- アプリ内でお金について気軽に質問・相談できる機能が欲しい
- 他の人のお金のやりくり・家計簿を見本にしたい
- 自分の家計簿を公開して、アドバイスが欲しい

これらの希望を実現できるアプリを作りたいと思い、Piggy Bankを作成しました。

 
 # 使用技術
  
- Java 11.0.7
- SpringBoot 2.3.5
  - Spring Security 5.3.5
  - Spring Data JPA 2.3.5
  - Spring Cloud for Amazon Web Services 2.2.5
- HTML(Thymeleaf), CSS
- MaterializeCSS 1.0.0
- JavaScript
  - jQuery 3.5.1
  - Chart.js 2.8.0
  - FullCalendar 3.5.1
- AWS
  - EC2
  - RDS
  - S3
  - ELB
  - Route53
  - ACM
- MySQL 8.0.20
- Nginx 1.18.0
- Tomcat 9.0.39

 
# 機能一覧
- 基本機能
  - 会員登録・退会機能
  - ログイン機能
  - ユーザー設定変更機能
  - ゲストログイン機能
- 家計簿機能
  - 予算設定機能
  - 出入金記録・編集・削除機能
  - 履歴表示機能（一覧形式）
  - 履歴表示機能（カレンダー形式）
  - 履歴絞込機能
  - 履歴ソート機能
  - 出入金分析機能（グラフ表示）
- 質問・相談機能
  - 質問&相談投稿・編集・削除機能
  - コメント投稿・編集・削除機能
  - お気に入り機能
  - カテゴリー別表示機能
  - 検索機能

 
# 工夫した点
### 家計簿機能
- 予算設定機能を導入することで、「あと何円使えるのか」をわかりやすく表示
- 絞込・ソート機能やカレンダー表示機能で、自分が確認したい履歴だけを表示できるようにした

[![Image from Gyazo](https://i.gyazo.com/8fbbdb9d6cd753ce73917b4faa42e0b8.gif)](https://gyazo.com/8fbbdb9d6cd753ce73917b4faa42e0b8)

[![Image from Gyazo](https://i.gyazo.com/4f3d9a44dc279d8f67a28dd20b706735.gif)](https://gyazo.com/4f3d9a44dc279d8f67a28dd20b706735)

### 質問・相談機能
- 後から読み返すことができるよう「お気に入り」機能を実装
- カテゴリ分け・検索機能を実装して、欲しい情報にすぐアクセスできるように
　
[![Image from Gyazo](https://i.gyazo.com/810e609ab4f31426091eb2c2af64d133.gif)](https://gyazo.com/810e609ab4f31426091eb2c2af64d133)

 
# DB設計
![PiggyBank-ER](https://user-images.githubusercontent.com/68217595/110123954-de54cb00-7e04-11eb-972b-07fa671da904.png)

### 各テーブルについて
| テーブル名 | 説明 |
|:-:|:-:|
| users | 登録ユーザーの情報 |
| money_records | ユーザーの家計簿データ |
| categories | 出入金カテゴリの情報 |
| posts | 質問・相談の投稿情報 |
| comments | 投稿対するコメントの情報 |
| likes | 投稿への「お気に入り」登録情報 |

 
# インフラ設計
　
- 今回はEC2、RDS、ELB、S3、Route53、ACMを利用しました。
- ELBはACMを使用するために導入しており、EC2冗長化等は行っておりません。
- S3については、ユーザーのアイコン画像を格納するために導入しています。
![infra](https://user-images.githubusercontent.com/68217595/110447290-6c7dc980-8103-11eb-9ec9-7cc39b703b8b.png)

 
# 今後追加したい機能等
### 機能面
- 質問・相談時の画像投稿機能（いますぐやりたい）
- ユーザー間のフォロー機能
- 家計簿機能で使用しているグラフ表示機能を質問・相談機能にも導入 （自分の家計簿を公開して相談したり、回答者が家計簿を提示してアドバイスできるようにしたい）
- レスポンシブ対応
### インフラ面
- EC2,RDSの冗長化
- Dockerを用いた開発・本番環境の構築
- CircleCIを用いたCI/CDパイプラインの構築
- セキュリティ対策

# 参考
https://qiita.com/kumaGoro_95/items/ad561e3e93ad61b2e63c
