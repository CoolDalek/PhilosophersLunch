object App extends Module {

  def main(args: Array[String]): Unit = {
    args.foreach(println)
    sys.env.foreach(println)
    sys.props.foreach(println)

  }

}