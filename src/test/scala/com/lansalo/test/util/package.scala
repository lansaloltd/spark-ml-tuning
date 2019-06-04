package com.lansalo.test

import java.io.File

package object util {

  def delete(file: File) {
    if (file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete)
    file.delete
  }

  /**
    * delete all the files in the given directory which name start with the given prefix
    * @param file
    * @param prefix
    */
  def delete(file: File, prefix: String) {
    if (file.isDirectory) {
      file.list().toList.filter(_.startsWith(prefix)).foreach(name => {
        delete(new File(file.getAbsolutePath + File.separator + name))
      })
    }
  }

}
