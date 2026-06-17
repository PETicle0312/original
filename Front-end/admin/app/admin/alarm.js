import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useRouter } from 'expo-router';
import { View, Text, StyleSheet, TouchableOpacity, Image, ScrollView } from 'react-native';
import AsyncStorage from "@react-native-async-storage/async-storage";

const petIcon = require('../../assets/images/pet_icon.png');
const arrowLeft = require('../../assets/images/arrow_left.png');

const BASE_URL = "http://172.18.38.26:8080";

const formatDate = (dateString) => {
  const date = new Date(dateString);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  let hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const ampm = hours >= 12 ? "PM" : "AM";

  hours = hours % 12;
  hours = hours ? hours : 12;

  return `${year}-${month}-${day} ${hours}:${minutes}${ampm}`;
};

export default function AlarmScreen() {
  const [todayAlarms, setTodayAlarms] = useState([]);
  const [oldAlarms, setOldAlarms] = useState([]);
  const router = useRouter();

  useEffect(() => {
    const fetchAlarms = async () => {
      try {
        const adminId = await AsyncStorage.getItem("adminId");
        if (!adminId) {
          console.warn("관리자 ID 없음 (로그인 필요)");
          return;
        }

        const res = await axios.get(`${BASE_URL}/api/admin/notifications?adminId=${adminId}`);
        const alarms = res.data;

        const now = new Date();
        const today = alarms.filter(a => {
          const created = new Date(a.logTime);
          return created.toDateString() === now.toDateString();
        });
        const old = alarms.filter(a => {
          const created = new Date(a.logTime);
          return created.toDateString() !== now.toDateString();
        });

        setTodayAlarms(today);
        setOldAlarms(old);
      } catch (err) {
        console.error("❌ 알림 불러오기 실패:", err);
      }
    };

    fetchAlarms();
  }, []);

  const onBack = () => {
    router.back();
  };

  const renderAlarm = (alarm) => {
    return (
      <View style={styles.alarmCard} key={alarm.logId}>
        <Image source={petIcon} style={styles.alarmIcon} />
        <View style={styles.alarmTextBox}>
          <View style={styles.alarmTitleRow}>
            <Text style={styles.alarmManager}>
              <Text style={{ fontWeight: 'bold' }}>관리자 {alarm.adminName}</Text>
            </Text>
            <Text style={styles.alarmTime}>
              {formatDate(alarm.logTime)}
            </Text>
          </View>
          <Text style={styles.alarmMsg}>
            {(alarm.schoolName ?? "알 수 없는 학교")}의 페티클이 {alarm.actionType ?? "알 수 없는 동작"} 되었습니다.
          </Text>
          <Text style={styles.alarmDevice}>페티클 번호 : {alarm.deviceId}</Text>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity style={{ width: 40, alignItems: 'flex-start' }} onPress={onBack}>
          <Image source={arrowLeft} style={styles.arrowIcon} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>알림</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={{ paddingBottom: 30 }}>
        <Text style={styles.sectionTitle}>오늘 받은 알림</Text>
        {todayAlarms.length > 0 ? (
          todayAlarms.map(renderAlarm)
        ) : (
          <Text style={{ marginLeft: 18, color: "#999" }}>오늘 알림이 없습니다.</Text>
        )}

        <Text style={[styles.sectionTitle, { marginTop: 28 }]}>이전 알림</Text>
        {oldAlarms.length > 0 ? (
          oldAlarms.map(renderAlarm)
        ) : (
          <Text style={{ marginLeft: 18, color: "#999" }}>이전 알림이 없습니다.</Text>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F6F6',
    paddingTop: 65,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingBottom: 16,
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  arrowIcon: {
    width: 20,
    height: 20,
    resizeMode: 'contain',
    tintColor: '#888',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#222',
    textAlign: 'center',
    flex: 1,
  },
  sectionTitle: {
    fontSize: 16,
    color: '#444',
    fontWeight: 'bold',
    marginBottom: 9,
    marginLeft: 18,
    marginTop: 15,
  },
  alarmCard: {
    backgroundColor: '#fff',
    borderRadius: 14,
    flexDirection: 'row',
    padding: 16,
    marginHorizontal: 16,
    marginBottom: 10,
    alignItems: 'flex-start',
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowRadius: 2,
    elevation: 1,
  },
  alarmIcon: {
    width: 42,
    height: 34,
    resizeMode: 'contain',
    marginRight: 10,
    marginTop: 8,
  },
  alarmTextBox: {
    flex: 1,
  },
  alarmTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 2,
  },
  alarmManager: {
    fontSize: 14,
    color: '#222',
    marginRight: 10,
  },
  alarmTime: {
    fontSize: 13,
    color: '#aaa',
    marginLeft: 4,
  },
  alarmMsg: {
    fontSize: 14,
    color: '#222',
    marginBottom: 2,
  },
  alarmDevice: {
    fontSize: 12,
    color: '#C5C5C5',
    marginTop: 1,
  },
});
