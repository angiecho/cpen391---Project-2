#include <stdbool.h>

#ifndef BLUETOOTH_H_
#define BLUETOOTH_H_

#include <stdbool.h>

#define Bluetooth_Status 		(*(volatile unsigned char *)(0x84000220))
#define Bluetooth_Control 		(*(volatile unsigned char *)(0x84000220))
#define Bluetooth_TxData 		(*(volatile unsigned char *)(0x84000222))
#define Bluetooth_RxData 		(*(volatile unsigned char *)(0x84000222))

#define Bluetooth_RS232_Control (*(volatile unsigned char *)(0x84000200))
#define Bluetooth_RS232_Status  (*(volatile unsigned char *)(0x84000200))
#define Bluetooth_RS232_TxData  (*(volatile unsigned char *)(0x84000202))
#define Bluetooth_RS232_RxData  (*(volatile unsigned char *)(0x84000202))
#define Bluetooth_RS232_Baud    (*(volatile unsigned char *)(0x84000204))


#define Bluetooth_Baud    		(*(volatile unsigned char *)(0x84000224))


#define LEFT_BT 0
#define RIGHT_BT 1

/**************************************************************************
***
**  Initialise bluetooth
*****************************************************************************/
void putCharBluetooth(char c, int curr);
char getCharBluetooth(int curr);
void Init_Bluetooth(void);
void WaitForReadStat();

void putCharBluetooth_RS232(char c);
char getCharBluetooth_RS232();


#endif /* BLUETOOTH_H_ */
