// telnet defaults to port 23
TCPServer server = TCPServer(23);
TCPClient client;
char myIpString[24];
#define MAX_DATA_SIZE 3000
unsigned char data[MAX_DATA_SIZE];
bool data_received = false;
void setup() {
    // start listening for clients
    server.begin();

    pinMode(D0, OUTPUT);
    pinMode(D1, OUTPUT);
    pinMode(D2, OUTPUT);
    pinMode(D3, OUTPUT);
    pinMode(D4, OUTPUT);
    pinMode(D5, OUTPUT);
    pinMode(D6, OUTPUT);
    pinMode(D7, OUTPUT);
    digitalWrite(D7, HIGH);
    IPAddress myIp = WiFi.localIP();
    sprintf(myIpString, "%d.%d.%d.%d", myIp[0], myIp[1], myIp[2], myIp[3]);
    Spark.variable("ipAddress", myIpString, STRING);

}
bool flipflag = false;
int data_counter = 0;
int data_size = 0;

void display_data(unsigned char c) {
    if(c & 0x01) digitalWrite(D0, HIGH);
    else digitalWrite(D0, LOW);
    
    if(c & 0x02) digitalWrite(D1, HIGH);
    else digitalWrite(D1, LOW);
    
    if(c & 0x04) digitalWrite(D2, HIGH);
    else digitalWrite(D2, LOW);
    
    if(c & 0x08) digitalWrite(D3, HIGH);
    else digitalWrite(D3, LOW);
    
    if(c & 0x10) digitalWrite(D4, HIGH);
    else digitalWrite(D4, LOW);
    
    if(c & 0x20) digitalWrite(D5, HIGH);
    else digitalWrite(D5, LOW);
}

void loop()
{
    if(!data_received) {
        if (client.connected()) {
            // echo all available bytes back to the client
            while (client.available()) {
                unsigned char recv = client.read();
                if(recv == 0x40) {
                    data_received = true;
                    break;
                } else {
                    if(data_size < MAX_DATA_SIZE) {
                        data[data_size] = recv;
                        ++data_size;
                    }
                }
                if(flipflag) digitalWrite(D7, HIGH);
                else digitalWrite(D7, LOW);
                flipflag = ! flipflag;
            }
        } else {
            // if no client is yet connected, check for a new connection
            client = server.available();
        }
    } else {
        if(data_counter < data_size) {
            display_data(data[data_counter]);
            ++data_counter;
            delay(96);
        } else {
            data_counter = 0;
            data_size = 0;
            data_received = false;
            digitalWrite(D0, LOW);
            digitalWrite(D1, LOW);
            digitalWrite(D2, LOW);
            digitalWrite(D3, LOW);
            digitalWrite(D4, LOW);
            digitalWrite(D5, LOW);
            digitalWrite(D6, LOW);
            digitalWrite(D7, LOW);
        }
    }
    
}