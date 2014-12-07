package main

import (
	"net"
	"os"
)

var data = [...]byte{0xa5, 0x5a, 0xa5, 0x5a, 0xa5, 0x5a, 64}

func main() {
	servAddr := "192.168.43.141:23"
	tcpAddr, err := net.ResolveTCPAddr("tcp", servAddr)
	if err != nil {
		println("ResolveTCPAddr failed:", err.Error())
		os.Exit(1)
	}

	conn, err := net.DialTCP("tcp", nil, tcpAddr)
	if err != nil {
		println("Dial failed:", err.Error())
		os.Exit(1)
	}

	_, err = conn.Write(data[:])
	if err != nil {
		println("Write to server failed:", err.Error())
		os.Exit(1)
	}

	println("write to server = ", string(data[:]))
	conn.Close()
}
